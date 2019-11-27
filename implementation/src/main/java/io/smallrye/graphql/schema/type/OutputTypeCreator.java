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

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.MethodParameterInfo;
import org.jboss.jandex.Type;
import org.jboss.logging.Logger;

import graphql.Scalars;
import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLTypeReference;
import io.smallrye.graphql.execution.AnnotatedPropertyDataFetcher;
import io.smallrye.graphql.execution.ReflectionDataFetcher;
import io.smallrye.graphql.index.Annotations;
import io.smallrye.graphql.schema.helper.AnnotationsHelper;
import io.smallrye.graphql.schema.helper.ArgumentsHelper;
import io.smallrye.graphql.schema.helper.DescriptionHelper;
import io.smallrye.graphql.schema.helper.IgnoreHelper;
import io.smallrye.graphql.schema.helper.NameHelper;
import io.smallrye.graphql.schema.helper.NonNullHelper;
import io.smallrye.graphql.schema.holder.AnnotationsHolder;
import io.smallrye.graphql.schema.holder.TypeHolder;

/**
 * Create a graphql-java GraphQLOutputType
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@ApplicationScoped
public class OutputTypeCreator {
    private static final Logger LOG = Logger.getLogger(OutputTypeCreator.class.getName());

    @Produces
    private final Map<DotName, GraphQLObjectType> outputObjectMap = new HashMap<>();

    @Inject
    private Map<DotName, GraphQLScalarType> scalarMap;

    @Inject
    private Map<DotName, GraphQLEnumType> enumMap;

    @Inject
    @Named("output")
    private Map<DotName, TypeHolder> outputClasses;

    @Inject
    private NameHelper nameHelper;

    @Inject
    private NonNullHelper nonNullHelper;

    @Inject
    private DescriptionHelper descriptionHelper;

    @Inject
    private IgnoreHelper ignoreHelper;

    @Inject
    private AnnotationsHelper annotationsHelper;

    @Inject
    private ArgumentsHelper argumentsHelper;

    @Inject
    private GraphQLCodeRegistry.Builder codeRegistryBuilder;

    @Inject
    private Map<DotName, List<MethodParameterInfo>> sourceFields;

    public GraphQLOutputType createGraphQLOutputType(Type type, AnnotationsHolder annotations) {
        if (nonNullHelper.markAsNonNull(type, annotations)) {
            return GraphQLNonNull.nonNull(toGraphQLOutputType(type, annotations));
        } else {
            return toGraphQLOutputType(type, annotations);
        }

        // TODO: Deprecate
        // fieldDefinitionBuilder.deprecate(description)

        // TODO: Directives ?
        // fieldDefinitionBuilder.withDirectives(directives) // TODO ?
    }

    @PostConstruct
    void createOutputObjectTypes() {
        for (Map.Entry<DotName, TypeHolder> e : outputClasses.entrySet()) {
            this.outputObjectMap.put(e.getKey(), createOutputObjectType(e.getValue()));
        }
    }

    private GraphQLObjectType createOutputObjectType(TypeHolder typeHolder) {
        String name = nameHelper.getOutputTypeName(typeHolder);
        ClassInfo classInfo = typeHolder.getClassInfo();

        GraphQLObjectType.Builder objectTypeBuilder = GraphQLObjectType.newObject();
        objectTypeBuilder = objectTypeBuilder.name(name);

        // Description
        Optional<String> maybeDescription = descriptionHelper.getDescription(typeHolder);
        objectTypeBuilder = objectTypeBuilder.description(maybeDescription.orElse(null));

        // Fields
        objectTypeBuilder = objectTypeBuilder.fields(createGraphQLFieldDefinitions(classInfo, name));

        return objectTypeBuilder.build();
    }

    private List<GraphQLFieldDefinition> createGraphQLFieldDefinitions(ClassInfo classInfo, String name) {
        List<GraphQLFieldDefinition> fieldDefinitions = new ArrayList<>();
        List<FieldInfo> fields = classInfo.fields();

        for (FieldInfo field : fields) {
            // Check if there is a getter (for output) 
            Optional<MethodInfo> maybeGetter = getGetMethod(field.name(), classInfo);
            if (maybeGetter.isPresent()) {
                MethodInfo getter = maybeGetter.get();
                // Annotations on the field and getter
                AnnotationsHolder annotations = annotationsHelper.getAnnotationsForField(field, getter);

                if (!ignoreHelper.shouldIgnore(annotations)) {
                    GraphQLFieldDefinition.Builder builder = getGraphQLFieldDefinitionBuilder(annotations, field.name(),
                            field.type());

                    GraphQLFieldDefinition graphQLFieldDefinition = builder.build();

                    codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(name, graphQLFieldDefinition.getName()),
                            new AnnotatedPropertyDataFetcher(field.name(), field.type(), annotations));

                    fieldDefinitions.add(graphQLFieldDefinition);

                }
            }
        }

        // Also check for @Source fields
        if (sourceFields.containsKey(classInfo.name())) {
            List<MethodParameterInfo> methodParameterInfos = sourceFields.get(classInfo.name());
            for (MethodParameterInfo methodParameterInfo : methodParameterInfos) {
                MethodInfo methodInfo = methodParameterInfo.method();

                // Annotations on this method
                AnnotationsHolder annotations = annotationsHelper.getAnnotationsForMethod(methodInfo);
                if (!ignoreHelper.shouldIgnore(annotations)) {

                    Type type = methodParameterInfo.method().returnType();
                    GraphQLFieldDefinition.Builder builder = getGraphQLFieldDefinitionBuilder(annotations, methodInfo.name(),
                            type);

                    // Arguments (input) TODO: Remove @source and add others
                    // builder.arguments(argumentsHelper.toGraphQLArguments(methodInfo, annotations));

                    // TODO: Check that the receiver is a CDI Bean ?
                    codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(name, methodInfo.name()),
                            new ReflectionDataFetcher(methodParameterInfo.method()));

                    fieldDefinitions.add(builder.build());
                }
            }
        }

        return fieldDefinitions;
    }

    private GraphQLFieldDefinition.Builder getGraphQLFieldDefinitionBuilder(AnnotationsHolder annotations, String fieldName,
            Type fieldType) {
        GraphQLFieldDefinition.Builder builder = GraphQLFieldDefinition.newFieldDefinition();
        // Name
        builder = builder.name(nameHelper.getOutputNameForField(annotations, fieldName));

        // Description
        Optional<String> maybeFieldDescription = descriptionHelper.getDescription(annotations, fieldType);
        builder = builder.description(maybeFieldDescription.orElse(null));

        // Type
        builder = builder
                .type(createGraphQLOutputType(fieldType, annotations));

        return builder;
    }

    private GraphQLOutputType toGraphQLOutputType(Type type, AnnotationsHolder annotations) {

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
            return GraphQLList.list(toGraphQLOutputType(typeInArray, annotations));
        } else if (type.kind().equals(Type.Kind.PARAMETERIZED_TYPE)) {
            // Collections
            Type typeInCollection = type.asParameterizedType().arguments().get(0);
            return GraphQLList.list(toGraphQLOutputType(typeInCollection, annotations));
        } else if (outputClasses.containsKey(type.name())) {
            String name = nameHelper.getOutputTypeName(outputClasses.get(type.name()));
            return GraphQLTypeReference.typeRef(name);
        } else {
            // Maps ? Intefaces ? Generics ?
            throw new RuntimeException("Don't know what to do with " + type);
        }
    }

    private Optional<MethodInfo> getGetMethod(String forField, ClassInfo classInfo) {
        String get = GET + forField;
        String is = IS + forField;
        List<MethodInfo> methods = classInfo.methods();
        for (MethodInfo methodInfo : methods) {
            if (methodInfo.name().equalsIgnoreCase(get) || methodInfo.name().equalsIgnoreCase(is)) {
                return Optional.of(methodInfo);
            }
        }
        return Optional.empty();
    }

    private static final String GET = "get";
    private static final String IS = "is";
}
