/*
 * Copyright 2020 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.smallrye.graphql.bootstrap.type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;
import org.jboss.logging.Logger;

import graphql.Scalars;
import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeReference;
import io.smallrye.graphql.bootstrap.Annotations;
import io.smallrye.graphql.bootstrap.Argument;
import io.smallrye.graphql.bootstrap.Classes;
import io.smallrye.graphql.bootstrap.ObjectBag;
import io.smallrye.graphql.bootstrap.datafetcher.AnnotatedPropertyDataFetcher;
import io.smallrye.graphql.bootstrap.schema.helper.AnnotationsHelper;
import io.smallrye.graphql.bootstrap.schema.helper.DefaultValueHelper;
import io.smallrye.graphql.bootstrap.schema.helper.DescriptionHelper;
import io.smallrye.graphql.bootstrap.schema.helper.IgnoreHelper;
import io.smallrye.graphql.bootstrap.schema.helper.NameHelper;
import io.smallrye.graphql.bootstrap.schema.helper.NonNullHelper;

/**
 * Create a graphql-java GraphQLInputType
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class InputTypeCreator implements Creator {
    private static final Logger LOG = Logger.getLogger(InputTypeCreator.class.getName());

    private final IndexView index;
    private final EnumTypeCreator enumTypeCreator;
    private final NameHelper nameHelper = new NameHelper();
    private final DescriptionHelper descriptionHelper = new DescriptionHelper();
    private final NonNullHelper nonNullHelper = new NonNullHelper();
    private final IgnoreHelper ignoreHelper = new IgnoreHelper();
    private final AnnotationsHelper annotationsHelper = new AnnotationsHelper();
    private final DefaultValueHelper defaultValueHelper = new DefaultValueHelper();

    private final ObjectBag objectBag;

    public InputTypeCreator(IndexView index, ObjectBag objectBag) {
        this.index = index;
        this.objectBag = objectBag;
        this.enumTypeCreator = new EnumTypeCreator(objectBag);
    }

    @Override
    public GraphQLType create(ClassInfo classInfo) {
        if (objectBag.getInputMap().containsKey(classInfo.name())) {
            return objectBag.getInputMap().get(classInfo.name());
        } else {
            Annotations annotations = annotationsHelper.getAnnotationsForClass(classInfo);
            String name = nameHelper.getInputTypeName(classInfo, annotations);

            GraphQLInputObjectType.Builder inputObjectTypeBuilder = GraphQLInputObjectType.newInputObject().name(name);

            // Description
            Optional<String> maybeDescription = descriptionHelper.getDescriptionForType(annotations);
            inputObjectTypeBuilder = inputObjectTypeBuilder.description(maybeDescription.orElse(null));

            // Fields
            inputObjectTypeBuilder = inputObjectTypeBuilder.fields(createGraphQLInputObjectField(classInfo, name));

            GraphQLInputObjectType graphQLInputObjectType = inputObjectTypeBuilder.build();
            objectBag.getInputMap().put(classInfo.name(), graphQLInputObjectType);

            return graphQLInputObjectType;
        }
    }

    public GraphQLInputType createGraphQLInputType(Type type, Annotations annotations) {
        return createGraphQLInputType(type, type, annotations);
    }

    private List<GraphQLInputObjectField> createGraphQLInputObjectField(ClassInfo classInfo, String name) {
        List<GraphQLInputObjectField> inputObjectFields = new ArrayList<>();
        // Fields
        List<FieldInfo> fields = classInfo.fields();

        final Map<String, String> customFieldNameMapping = new HashMap<>();
        final Map<String, Argument> fieldAnnotationsMapping = new HashMap<>();
        for (FieldInfo field : fields) {
            // Check if there is a setter (for input) 
            Optional<MethodInfo> maybeSetter = getSetMethod(field.name(), classInfo);
            if (maybeSetter.isPresent()) {
                MethodInfo setter = maybeSetter.get();
                // Annotations on the field and setter
                Annotations annotations = annotationsHelper.getAnnotationsForInputField(field, setter);
                if (!ignoreHelper.shouldIgnore(annotations)) {
                    GraphQLInputObjectField.Builder builder = GraphQLInputObjectField.newInputObjectField();

                    // Name
                    String fieldName = nameHelper.getInputNameForField(annotations, field.name());
                    builder = builder.name(fieldName);

                    // Description
                    Optional<String> maybeFieldDescription = descriptionHelper.getDescriptionForField(annotations,
                            field.type());
                    builder = builder.description(maybeFieldDescription.orElse(null));

                    // Type
                    builder = builder
                            .type(createGraphQLInputType(field.type(), setter.parameters().get(0), annotations));

                    objectBag.getCodeRegistryBuilder().dataFetcher(FieldCoordinates.coordinates(name, fieldName),
                            new AnnotatedPropertyDataFetcher(field.name(), field.type(), annotations));

                    // Default value (on method)
                    Annotations annotationsForThisArgument = annotationsHelper.getAnnotationsForArgument(setter);
                    Optional<Object> maybeDefaultValue = defaultValueHelper.getDefaultValue(annotationsForThisArgument,
                            annotations);
                    builder = builder.defaultValue(maybeDefaultValue.orElse(null));

                    inputObjectFields.add(builder.build());

                    // Name mapping for input transformation
                    if (!field.name().equals(fieldName)) {
                        customFieldNameMapping.put(field.name(), fieldName);
                    }
                    // Other annotation for other transformation
                    if (annotations.hasGraphQLFormatingAnnotations()) {
                        Optional<String> description = descriptionHelper.getDescriptionForField(annotationsForThisArgument,
                                field.type());
                        Argument a = new Argument();
                        a.setName(fieldName);
                        a.setAnnotations(annotations); // TODO: Should this not be annotationsForThisArgument
                        a.setType(field.type());
                        a.setDescription(description.orElse(null));
                        fieldAnnotationsMapping.put(fieldName, a);
                    }
                }
            }
        }

        // So that we can do transformations on input that can not be done with Jsonb
        if (!fieldAnnotationsMapping.isEmpty()) {
            objectBag.getArgumentMap().put(classInfo.name(), fieldAnnotationsMapping);
        }

        // So that we can rename fields
        objectBag.getInputJsonMap().put(classInfo.name(), createJsonb(customFieldNameMapping));

        return inputObjectFields;
    }

    private GraphQLInputType createGraphQLInputType(Type type, Type setterParameterType, Annotations annotations) {
        if (nonNullHelper.markAsNonNull(type, annotations)) {
            return GraphQLNonNull.nonNull(toGraphQLInputType(type, setterParameterType, annotations));
        } else {
            return toGraphQLInputType(type, setterParameterType, annotations);
        }
    }

    private Jsonb createJsonb(Map<String, String> customFieldNameMapping) {
        JsonbConfig config = new JsonbConfig()
                .withNullValues(Boolean.TRUE)
                .withFormatting(Boolean.TRUE);

        if (!customFieldNameMapping.isEmpty()) {
            config = config.withPropertyNamingStrategy(new GraphQLNamingStrategy(customFieldNameMapping));
        }

        return JsonbBuilder.create(config);
    }

    private GraphQLInputType toGraphQLInputType(Type type, Type setterParameterType, Annotations annotations) {

        DotName fieldTypeName = type.name();

        if (annotations.containsOneOfTheseKeys(Annotations.ID)) {
            // ID
            return Scalars.GraphQLID;
        } else if (objectBag.getScalarMap().containsKey(fieldTypeName)) {
            // Scalar
            return objectBag.getScalarMap().get(fieldTypeName);
        } else if (type.kind().equals(Type.Kind.ARRAY)) {
            // Array 
            Type typeInArray = type.asArrayType().component();
            Type typeInParameter = setterParameterType.asArrayType().component();
            return toParameterizedGraphQLInputType(typeInArray, typeInParameter, annotations);
        } else if (type.kind().equals(Type.Kind.PARAMETERIZED_TYPE)) {
            // Collections
            Type typeInCollection = type.asParameterizedType().arguments().get(0);
            Type typeInParameter = setterParameterType.asParameterizedType().arguments().get(0);
            return toParameterizedGraphQLInputType(typeInCollection, typeInParameter, annotations);
        } else if (type.kind().equals(Type.Kind.CLASS)) {

            ClassInfo classInfo = index.getClassByName(type.name());
            if (classInfo != null) {
                if (Classes.isEnum(classInfo)) {
                    return enumTypeCreator.create(classInfo);
                } else if (objectBag.getInputMap().containsKey(classInfo.name())) {
                    return objectBag.getInputMap().get(classInfo.name());
                } else {
                    Annotations annotationsForThisClass = annotationsHelper.getAnnotationsForClass(classInfo);
                    String name = nameHelper.getInputTypeName(classInfo, annotationsForThisClass);
                    return GraphQLTypeReference.typeRef(name);
                }
            } else {
                LOG.warn("Class [" + type.name()
                        + "] in not indexed in Jandex. Can not create Input, defaulting to String Scalar");
                return Scalars.GraphQLString; // default
            }
        } else {
            throw new SchemaTypeCreateException("Don't know what to do with [" + type + "] of kind [" + type.kind() + "]");
        }
    }

    private GraphQLInputType toParameterizedGraphQLInputType(Type typeInCollection, Type setterParameterType,
            Annotations annotations) {
        Annotations annotationsInParameterizedType = annotationsHelper.getAnnotationsForType(typeInCollection,
                setterParameterType);
        if (nonNullHelper.markAsNonNull(typeInCollection, annotationsInParameterizedType, true)) {
            return GraphQLList
                    .list(GraphQLNonNull.nonNull(toGraphQLInputType(typeInCollection, setterParameterType, annotations)));
        } else {
            return GraphQLList.list(toGraphQLInputType(typeInCollection, setterParameterType, annotations));
        }
    }

    private Optional<MethodInfo> getSetMethod(String forField, ClassInfo classInfo) {
        String name = SET + forField;
        List<MethodInfo> methods = classInfo.methods();
        for (MethodInfo methodInfo : methods) {
            if (methodInfo.name().equalsIgnoreCase(name)) {
                return Optional.of(methodInfo);
            }
        }
        return Optional.empty();
    }

    private static final String SET = "set";
}
