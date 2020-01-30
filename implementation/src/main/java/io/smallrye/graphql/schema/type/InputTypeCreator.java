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
package io.smallrye.graphql.schema.type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.Index;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;
import org.jboss.logging.Logger;

import graphql.Scalars;
import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeReference;
import io.smallrye.graphql.execution.datafetchers.AnnotatedPropertyDataFetcher;
import io.smallrye.graphql.schema.Annotations;
import io.smallrye.graphql.schema.Argument;
import io.smallrye.graphql.schema.Classes;
import io.smallrye.graphql.schema.helper.AnnotationsHelper;
import io.smallrye.graphql.schema.helper.DefaultValueHelper;
import io.smallrye.graphql.schema.helper.DescriptionHelper;
import io.smallrye.graphql.schema.helper.IgnoreHelper;
import io.smallrye.graphql.schema.helper.NameHelper;
import io.smallrye.graphql.schema.helper.NonNullHelper;

/**
 * Create a graphql-java GraphQLInputType
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@ApplicationScoped
public class InputTypeCreator implements Creator {
    private static final Logger LOG = Logger.getLogger(InputTypeCreator.class.getName());

    @Inject
    private EnumTypeCreator enumTypeCreator;

    @Produces
    private final Map<DotName, Jsonb> inputJsonbMap = new HashMap<>();

    @Produces
    private final Map<DotName, Map<String, Argument>> argumentMap = new HashMap<>();

    @Inject
    private Map<DotName, GraphQLScalarType> scalarMap;

    @Inject
    private Index index;

    @Inject
    private NameHelper nameHelper;

    @Inject
    private DescriptionHelper descriptionHelper;

    @Inject
    private NonNullHelper nonNullHelper;

    @Inject
    private IgnoreHelper ignoreHelper;

    @Inject
    private AnnotationsHelper annotationsHelper;

    @Inject
    private DefaultValueHelper defaultValueHelper;

    @Inject
    private GraphQLCodeRegistry.Builder codeRegistryBuilder;

    @Override
    public GraphQLType create(ClassInfo classInfo, Annotations annotations) {
        String name = nameHelper.getInputTypeName(classInfo, annotations);

        GraphQLInputObjectType.Builder inputObjectTypeBuilder = GraphQLInputObjectType.newInputObject().name(name);

        // Description
        Optional<String> maybeDescription = descriptionHelper.getDescription(annotations);
        inputObjectTypeBuilder = inputObjectTypeBuilder.description(maybeDescription.orElse(null));

        // Fields
        inputObjectTypeBuilder = inputObjectTypeBuilder.fields(createGraphQLInputObjectField(classInfo, name));

        return inputObjectTypeBuilder.build();
    }

    public GraphQLInputType createGraphQLInputType(Type type, Annotations annotations) {
        return createGraphQLInputType(type, type, annotations);
    }

    private List<GraphQLInputObjectField> createGraphQLInputObjectField(ClassInfo classInfo, String name) {
        List<GraphQLInputObjectField> inputObjectFields = new ArrayList<>();
        // Fields (TODO: Look at methods rather ? Or both ?)
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
                    Optional<String> maybeFieldDescription = descriptionHelper.getDescription(annotations, field);
                    builder = builder.description(maybeFieldDescription.orElse(null));

                    // Type
                    builder = builder
                            .type(createGraphQLInputType(field.type(), setter.parameters().get(0), annotations));

                    codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(name, fieldName),
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
                        fieldAnnotationsMapping.put(fieldName, new Argument(fieldName, field.type(), annotations));
                    }
                }
            }
        }
        // TODO: See if we can combine the 2 maps below.

        // So that we can do transformations on input that can not be done with Jsonb
        if (!fieldAnnotationsMapping.isEmpty()) {
            this.argumentMap.put(classInfo.name(), fieldAnnotationsMapping);
        }

        // So that we can rename fields
        this.inputJsonbMap.put(classInfo.name(), createJsonb(customFieldNameMapping));

        return inputObjectFields;
    }

    private GraphQLInputType createGraphQLInputType(Type type, Type setterParameterType, Annotations annotations) {
        if (nonNullHelper.markAsNonNull(type, annotations)) {
            return GraphQLNonNull.nonNull(toGraphQLInputType(type, setterParameterType, annotations));
        } else {
            return toGraphQLInputType(type, setterParameterType, annotations);
        }

        // TODO: Deprecate
        // fieldDefinitionBuilder.deprecate(description)

        // TODO: Directives ?
        // fieldDefinitionBuilder.withDirectives(directives) // TODO ?
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
        } else if (scalarMap.containsKey(fieldTypeName)) {
            // Scalar
            return scalarMap.get(fieldTypeName);
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
                Annotations annotationsForThisClass = annotationsHelper.getAnnotationsForClass(classInfo);

                if (Classes.isEnum(classInfo)) {
                    return enumTypeCreator.create(classInfo, annotationsForThisClass);
                } else {
                    String name = nameHelper.getInputTypeName(classInfo, annotationsForThisClass);
                    return GraphQLTypeReference.typeRef(name);
                }
            } else {
                LOG.warn("Class [" + type.name()
                        + "] in not indexed in Jandex. Can not create Input, defaulting to String Scalar");
                return Scalars.GraphQLString; // default
            }
        } else {
            // Maps ? Intefaces ? Generics ?
            throw new RuntimeException("Don't know what to do with [" + type + "] of kind [" + type.kind() + "]");
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
