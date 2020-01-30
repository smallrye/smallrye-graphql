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

package io.smallrye.graphql.schema;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.bind.Jsonb;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.Index;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;
import org.jboss.logging.Logger;

import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLType;
import io.smallrye.graphql.execution.datafetchers.ReflectionDataFetcher;
import io.smallrye.graphql.schema.helper.AnnotationsHelper;
import io.smallrye.graphql.schema.helper.ArgumentsHelper;
import io.smallrye.graphql.schema.helper.DescriptionHelper;
import io.smallrye.graphql.schema.helper.NameHelper;
import io.smallrye.graphql.schema.type.Creator;
import io.smallrye.graphql.schema.type.EnumTypeCreator;
import io.smallrye.graphql.schema.type.InputTypeCreator;
import io.smallrye.graphql.schema.type.OutputTypeCreator;

/**
 * Creates the GraphQL Schema
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@ApplicationScoped
public class GraphQLSchemaInitializer {
    private static final Logger LOG = Logger.getLogger(GraphQLSchemaInitializer.class.getName());

    @Inject
    private Index index;

    @Inject
    private OutputTypeCreator outputTypeCreator;

    @Inject
    private InputTypeCreator inputTypeCreator;

    @Inject
    private EnumTypeCreator enumTypeCreator;

    @Inject
    private Map<DotName, GraphQLScalarType> scalarMap;

    private final Map<DotName, GraphQLType> inputMap = new HashMap<>();
    private final Map<DotName, GraphQLType> typeMap = new HashMap<>();
    private final Map<DotName, GraphQLType> enumMap = new HashMap<>();

    @Inject
    @ConfigProperty(name = "mp.graphql.queryRootDescription", defaultValue = "Query root")
    private String queryRootDescription;

    @Inject
    @ConfigProperty(name = "mp.graphql.mutationRootDescription", defaultValue = "Mutation root")
    private String mutationRootDescription;

    @Inject
    private AnnotationsHelper annotationsHelper;

    @Inject
    private ArgumentsHelper argumentsHelper;

    @Inject
    private NameHelper nameHelper;

    @Inject
    private DescriptionHelper descriptionHelper;

    @Inject
    private GraphQLCodeRegistry.Builder codeRegistryBuilder;

    @Inject
    private Map<DotName, Jsonb> inputJsonbMap;

    @Inject
    private Map<DotName, Map<String, Argument>> argumentMap;

    private GraphQLSchema graphQLSchema;

    @PostConstruct
    public void init() {
        GraphQLObjectType.Builder queryBuilder = GraphQLObjectType.newObject()
                .name(Annotations.QUERY.withoutPackagePrefix())
                .description(queryRootDescription);

        GraphQLObjectType.Builder mutationBuilder = GraphQLObjectType.newObject()
                .name(Annotations.MUTATION.withoutPackagePrefix())
                .description(mutationRootDescription);

        List<AnnotationInstance> graphQLApiAnnotations = this.index.getAnnotations(Annotations.GRAPHQL_API);

        for (AnnotationInstance graphQLApiAnnotation : graphQLApiAnnotations) {
            ClassInfo apiClass = graphQLApiAnnotation.target().asClass();
            List<MethodInfo> methods = apiClass.methods();
            for (MethodInfo method : methods) {

                Annotations annotationsForMethod = annotationsHelper.getAnnotationsForMethod(method,
                        AnnotationTarget.Kind.METHOD);
                if (annotationsForMethod.containsOneOfTheseKeys(Annotations.QUERY)) {
                    queryBuilder = addField(queryBuilder, annotationsForMethod, Annotations.QUERY);
                }
                if (annotationsForMethod.containsOneOfTheseKeys(Annotations.MUTATION)) {
                    mutationBuilder = addField(mutationBuilder, annotationsForMethod, Annotations.MUTATION);
                }
            }
        }

        this.graphQLSchema = createGraphQLSchema(queryBuilder.build(), mutationBuilder.build());
    }

    public GraphQLSchema getGraphQLSchema() {
        return this.graphQLSchema;
    }

    private GraphQLObjectType.Builder addField(GraphQLObjectType.Builder builder, Annotations annotationsForMethod,
            DotName annotationToScan) {
        AnnotationInstance queryAnnotation = annotationsForMethod.getAnnotation(annotationToScan);
        GraphQLFieldDefinition graphQLFieldDefinition = getGraphQLFieldDefinition(queryAnnotation);
        builder = builder.field(graphQLFieldDefinition);
        MethodInfo methodInfo = queryAnnotation.target().asMethod();
        codeRegistryBuilder.dataFetcher(
                FieldCoordinates.coordinates(annotationToScan.withoutPackagePrefix(),
                        graphQLFieldDefinition.getName()),
                new ReflectionDataFetcher(methodInfo, argumentsHelper.toArguments(methodInfo),
                        inputJsonbMap, argumentMap, scalarMap));
        //        new LambdaMetafactoryDataFetcher(methodInfo));
        //                    PropertyDataFetcher.fetching(methodInfo.name()));

        // TODO: What if getting has void ?
        if (!methodInfo.returnType().kind().equals(Type.Kind.VOID)) {
            scanType(methodInfo.returnType(), this.typeMap, this.outputTypeCreator);
        }
        // arguments on getters and setter
        List<Type> parameters = methodInfo.parameters();
        for (Type parameter : parameters) {
            scanType(parameter, this.inputMap, this.inputTypeCreator);
        }

        return builder;
    }

    private GraphQLSchema createGraphQLSchema(GraphQLObjectType query, GraphQLObjectType mutation) {

        GraphQLSchema.Builder schemaBuilder = GraphQLSchema.newSchema();

        //schemaBuilder = schemaBuilder.clearAdditionalTypes();

        Set<GraphQLType> additionalTypes = new HashSet<>();
        additionalTypes.addAll(enumMap.values());
        additionalTypes.addAll(typeMap.values());
        additionalTypes.addAll(inputMap.values());
        schemaBuilder = schemaBuilder.additionalTypes(additionalTypes);

        schemaBuilder = schemaBuilder.query(query);
        schemaBuilder = schemaBuilder.mutation(mutation);

        schemaBuilder = schemaBuilder.codeRegistry(codeRegistryBuilder.build());

        return schemaBuilder.build();
    }

    private GraphQLFieldDefinition getGraphQLFieldDefinition(AnnotationInstance graphQLAnnotation) {
        MethodInfo methodInfo = graphQLAnnotation.target().asMethod();

        // Annotations on this method
        Annotations annotations = annotationsHelper.getAnnotationsForMethod(methodInfo);

        Type returnType = methodInfo.returnType();

        // Fields
        GraphQLFieldDefinition.Builder builder = GraphQLFieldDefinition.newFieldDefinition();
        // Name // TODO: Need test cases for this
        String fieldName = nameHelper.getExecutionTypeName(graphQLAnnotation, annotations);
        builder = builder.name(fieldName);

        // Description
        Optional<String> maybeDescription = descriptionHelper.getDescription(annotations);
        builder = builder.description(maybeDescription.orElse(null));

        // Type (output)
        builder = builder
                .type(outputTypeCreator.createGraphQLOutputType(returnType, annotations));

        // Arguments (input)
        builder.arguments(argumentsHelper.toGraphQLArguments(methodInfo, annotations));

        return builder.build();
    }

    private void scanType(Type type, Map<DotName, GraphQLType> map, Creator creator) {
        switch (type.kind()) {
            case ARRAY:
                Type typeInArray = type.asArrayType().component();
                scanType(typeInArray, map, creator);
                break;
            case PARAMETERIZED_TYPE:
                // TODO: Check if there is more than one type in the Collection, throw an exception ?
                Type typeInCollection = type.asParameterizedType().arguments().get(0);
                scanType(typeInCollection, map, creator);
                break;
            case PRIMITIVE:
                if (!scalarMap.containsKey(type.name())) {
                    LOG.warn("No scalar mapping for " + type.name() + " with kind " + type.kind());
                }
                break;
            case CLASS:
                if (!scalarMap.containsKey(type.name())) {
                    ClassInfo classInfo = index.getClassByName(type.name());
                    if (classInfo != null) {
                        scanClass(classInfo, map, creator);
                    } else {
                        LOG.warn("Not indexed class " + type.name() + " with kind " + type.kind());
                    }
                }
                break;
            default:
                LOG.error("What should we do with field type of " + type.name() + " with kind " + type.kind());
                break;
        }
    }

    private void scanClass(ClassInfo classInfo, Map<DotName, GraphQLType> map, Creator creator) {

        if (Classes.isEnum(classInfo)) {
            scanEnum(classInfo);
        } else {
            // Annotations on the field and getter
            Annotations annotationsForThisClass = annotationsHelper.getAnnotationsForClass(classInfo);

            if (!map.containsKey(classInfo.name())) {
                map.put(classInfo.name(), creator.create(classInfo, annotationsForThisClass));
                scanFieldsAndMethods(classInfo, map, creator);
            }
        }
    }

    private void scanEnum(ClassInfo classInfo) {

        // Annotations on the field and getter
        Annotations annotationsForThisClass = annotationsHelper.getAnnotationsForClass(classInfo);

        if (Classes.isEnum(classInfo)) {
            if (!enumMap.containsKey(classInfo.name())) {
                enumMap.put(classInfo.name(), enumTypeCreator.create(classInfo, annotationsForThisClass));
            }
        }
    }

    private void scanFieldsAndMethods(ClassInfo classInfo, Map<DotName, GraphQLType> map, Creator creator) {
        // fields
        List<FieldInfo> fieldInfos = classInfo.fields();
        for (FieldInfo fieldInfo : fieldInfos) {
            Type type = fieldInfo.type();
            scanType(type, map, creator);
        }

        // methods
        List<MethodInfo> methodInfos = classInfo.methods();
        for (MethodInfo methodInfo : methodInfos) {
            String methodName = methodInfo.name();

            // return types on getters and setters
            if (isSetter(methodName) || isGetter(methodName)) {
                // TODO: What if getting has void ?
                if (!methodInfo.returnType().kind().equals(Type.Kind.VOID)) {
                    scanType(methodInfo.returnType(), map, creator);
                }

                // arguments on getters and setter
                List<Type> parameters = methodInfo.parameters();
                for (Type parameter : parameters) {
                    scanType(parameter, map, creator);
                }
            }
        }
    }

    private boolean isSetter(String methodName) {
        return methodName.length() > 3 && methodName.startsWith(SET);
    }

    private boolean isGetter(String methodName) {
        return (methodName.length() > 3 && methodName.startsWith(GET))
                || (methodName.length() > 2 && methodName.startsWith(IS));
    }

    private static final String SET = "set";
    private static final String GET = "get";
    private static final String IS = "is";
}
