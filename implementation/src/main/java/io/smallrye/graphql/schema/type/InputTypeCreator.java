/*
 * Copyright 2019 Red Hat, Inc.
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

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;
import org.jboss.logging.Logger;

import graphql.Scalars;
import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLTypeReference;
import io.smallrye.graphql.execution.GraphQLNamingStrategy;
import io.smallrye.graphql.execution.datafetchers.AnnotatedPropertyDataFetcher;
import io.smallrye.graphql.index.Annotations;
import io.smallrye.graphql.schema.helper.AnnotationsHelper;
import io.smallrye.graphql.schema.helper.DefaultValueHelper;
import io.smallrye.graphql.schema.helper.DescriptionHelper;
import io.smallrye.graphql.schema.helper.IgnoreHelper;
import io.smallrye.graphql.schema.helper.NameHelper;
import io.smallrye.graphql.schema.helper.NonNullHelper;
import io.smallrye.graphql.schema.holder.AnnotationsHolder;
import io.smallrye.graphql.schema.holder.TypeHolder;

/**
 * Create a graphql-java GraphQLInputType
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@ApplicationScoped
public class InputTypeCreator {
    private static final Logger LOG = Logger.getLogger(InputTypeCreator.class.getName());

    @Produces
    private final Map<DotName, GraphQLInputObjectType> inputObjectMap = new HashMap<>();

    @Produces
    private final Map<DotName, Jsonb> inputJsonbMap = new HashMap<>();

    @Inject
    private Map<DotName, GraphQLScalarType> scalarMap;

    @Inject
    private Map<DotName, GraphQLEnumType> enumMap;

    @Inject
    @Named("input")
    private Map<DotName, TypeHolder> inputClasses;

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

    public GraphQLInputType createGraphQLInputType(Type type, AnnotationsHolder annotations) {
        if (nonNullHelper.markAsNonNull(type, annotations)) {
            return GraphQLNonNull.nonNull(toGraphQLInputType(type, annotations));
        } else {
            return toGraphQLInputType(type, annotations);
        }

        // TODO: Deprecate
        // fieldDefinitionBuilder.deprecate(description)

        // TODO: Directives ?
        // fieldDefinitionBuilder.withDirectives(directives) // TODO ?
    }

    @PostConstruct
    void init() {
        for (Map.Entry<DotName, TypeHolder> e : inputClasses.entrySet()) {
            this.inputObjectMap.put(e.getKey(), createInputObjectType(e.getValue()));
            //this.inputJsonbMap.put(e.getKey(), createJsonb(inputObjectType));
        }
    }

    private GraphQLInputObjectType createInputObjectType(TypeHolder typeHolder) {
        String name = nameHelper.getInputTypeName(typeHolder);
        ClassInfo classInfo = typeHolder.getClassInfo();

        GraphQLInputObjectType.Builder inputObjectTypeBuilder = GraphQLInputObjectType.newInputObject().name(name);

        // Description
        Optional<String> maybeDescription = descriptionHelper.getDescription(typeHolder);
        inputObjectTypeBuilder = inputObjectTypeBuilder.description(maybeDescription.orElse(null));

        // Fields
        inputObjectTypeBuilder = inputObjectTypeBuilder.fields(createGraphQLInputObjectField(classInfo, name));

        return inputObjectTypeBuilder.build();
    }

    private List<GraphQLInputObjectField> createGraphQLInputObjectField(ClassInfo classInfo, String name) {
        List<GraphQLInputObjectField> inputObjectFields = new ArrayList<>();
        // Fields (TODO: Look at methods rather ? Or both ?)
        List<FieldInfo> fields = classInfo.fields();
        short count = 0;
        Map<String, String> customFieldNameMapping = new HashMap<>();
        for (FieldInfo field : fields) {
            // Check if there is a setter (for input) 
            Optional<MethodInfo> maybeSetter = getSetMethod(field.name(), classInfo);
            if (maybeSetter.isPresent()) {
                MethodInfo setter = maybeSetter.get();
                // Annotations on the field and setter
                AnnotationsHolder annotations = annotationsHelper.getAnnotationsForField(field, setter);
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
                            .type(createGraphQLInputType(field.type(), annotations));

                    codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(name, fieldName),
                            new AnnotatedPropertyDataFetcher(field.name(), field.type(), annotations));

                    // Default value (on method)
                    AnnotationsHolder annotationsForThisArgument = annotationsHelper.getAnnotationsForArgument(setter, count);
                    Optional<Object> maybeDefaultValue = defaultValueHelper.getDefaultValue(annotationsForThisArgument,
                            annotations);
                    builder = builder.defaultValue(maybeDefaultValue.orElse(null));

                    inputObjectFields.add(builder.build());

                    if (!field.name().equals(fieldName)) {
                        customFieldNameMapping.put(field.name(), fieldName);
                    }
                }
            }

            count++;
        }

        this.inputJsonbMap.put(classInfo.name(), createJsonb(customFieldNameMapping));

        return inputObjectFields;
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

    private GraphQLInputType toGraphQLInputType(Type type, AnnotationsHolder annotations) {

        DotName fieldTypeName = type.name();

        if (annotations.containsOnOfTheseKeys(Annotations.ID)) {
            // ID
            return Scalars.GraphQLID;
        } else if (scalarMap.containsKey(fieldTypeName)) {
            // Scalar
            return scalarMap.get(fieldTypeName);
        } else if (enumMap.containsKey(fieldTypeName)) {
            // Enum  
            return enumMap.get(fieldTypeName);
        } else if (type.kind().equals(Type.Kind.ARRAY)) {
            // Array 
            Type typeInArray = type.asArrayType().component();
            return GraphQLList.list(toGraphQLInputType(typeInArray, annotations));
        } else if (type.kind().equals(Type.Kind.PARAMETERIZED_TYPE)) {
            // Collections
            Type typeInCollection = type.asParameterizedType().arguments().get(0);
            return GraphQLList.list(toGraphQLInputType(typeInCollection, annotations));
        } else if (inputClasses.containsKey(type.name())) {
            String name = nameHelper.getInputTypeName(inputClasses.get(type.name()));
            return GraphQLTypeReference.typeRef(name);
        } else {
            // Maps ? Intefaces ? Generics ?
            throw new RuntimeException("Don't know what to do with " + type);
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
