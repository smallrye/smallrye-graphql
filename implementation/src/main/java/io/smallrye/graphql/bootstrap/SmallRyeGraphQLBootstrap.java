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

package io.smallrye.graphql.bootstrap;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;
import org.jboss.logging.Logger;

import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLType;
import io.smallrye.graphql.bootstrap.datafetcher.ReflectionDataFetcher;
import io.smallrye.graphql.bootstrap.schema.VoidReturnNotAllowedException;
import io.smallrye.graphql.bootstrap.schema.helper.AnnotationsHelper;
import io.smallrye.graphql.bootstrap.schema.helper.ArgumentsHelper;
import io.smallrye.graphql.bootstrap.schema.helper.DescriptionHelper;
import io.smallrye.graphql.bootstrap.schema.helper.NameHelper;
import io.smallrye.graphql.bootstrap.type.Creator;
import io.smallrye.graphql.bootstrap.type.EnumTypeCreator;
import io.smallrye.graphql.bootstrap.type.InputTypeCreator;
import io.smallrye.graphql.bootstrap.type.OutputTypeCreator;

/**
 * Bootstrap MicroProfile GraphQL
 * This scan all classes for annotations and create the GraphQL Schema.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class SmallRyeGraphQLBootstrap {

    private static final Logger LOG = Logger.getLogger(SmallRyeGraphQLBootstrap.class.getName());

    private final EnumTypeCreator enumTypeCreator;

    private final AnnotationsHelper annotationsHelper = new AnnotationsHelper();
    private final ArgumentsHelper argumentsHelper = new ArgumentsHelper();
    private final NameHelper nameHelper = new NameHelper();
    private final DescriptionHelper descriptionHelper = new DescriptionHelper();

    private final IndexView index;
    private final OutputTypeCreator outputTypeCreator;
    private final InputTypeCreator inputTypeCreator;

    private final ObjectBag objectBag = new ObjectBag();

    public static GraphQLSchema bootstrap(IndexView index) {
        SmallRyeGraphQLBootstrap graphQLBootstrap = new SmallRyeGraphQLBootstrap(index);
        return graphQLBootstrap.generateGraphQLSchema();
    }

    private SmallRyeGraphQLBootstrap(IndexView index) {
        this.index = index;
        this.inputTypeCreator = new InputTypeCreator(index, objectBag);
        this.outputTypeCreator = new OutputTypeCreator(index, inputTypeCreator, objectBag);
        this.enumTypeCreator = new EnumTypeCreator(objectBag);
    }

    private GraphQLSchema generateGraphQLSchema() {
        GraphQLObjectType.Builder queryBuilder = GraphQLObjectType.newObject()
                .name(Annotations.QUERY.withoutPackagePrefix())
                .description("Query root");

        GraphQLObjectType.Builder mutationBuilder = GraphQLObjectType.newObject()
                .name(Annotations.MUTATION.withoutPackagePrefix())
                .description("Mutation root");

        Collection<AnnotationInstance> graphQLApiAnnotations = this.index.getAnnotations(Annotations.GRAPHQL_API);

        for (AnnotationInstance graphQLApiAnnotation : graphQLApiAnnotations) {
            ClassInfo apiClass = graphQLApiAnnotation.target().asClass();
            List<MethodInfo> methods = apiClass.methods();
            for (MethodInfo method : methods) {

                Annotations annotationsForMethod = annotationsHelper.getAnnotationsForMethod(method);
                if (annotationsForMethod.containsOneOfTheseKeys(Annotations.QUERY)) {
                    queryBuilder = addField(queryBuilder, annotationsForMethod, Annotations.QUERY);
                }
                if (annotationsForMethod.containsOneOfTheseKeys(Annotations.MUTATION)) {
                    mutationBuilder = addField(mutationBuilder, annotationsForMethod, Annotations.MUTATION);
                }
            }
        }

        // Let's see what still needs to be done.
        while (!objectBag.getTypeTodoList().isEmpty()) { // Don't iterate to prevent ConcurrentModificationException
            ClassInfo todo = objectBag.getTypeTodoList().remove(0);
            if (!objectBag.getTypeMap().containsKey(todo.name())) {
                outputTypeCreator.create(todo);
            }
        }

        while (!objectBag.getInputTypeTodoList().isEmpty()) { // Don't iterate to prevent ConcurrentModificationException
            ClassInfo todo = objectBag.getInputTypeTodoList().remove(0);
            if (!objectBag.getInputMap().containsKey(todo.name())) {
                inputTypeCreator.create(todo);
            }
        }

        return createGraphQLSchema(queryBuilder.build(), mutationBuilder.build());
    }

    private GraphQLObjectType.Builder addField(GraphQLObjectType.Builder builder,
            Annotations annotationsForMethod,
            DotName annotationToScan) {
        AnnotationInstance queryAnnotation = annotationsForMethod.getAnnotation(annotationToScan);
        GraphQLFieldDefinition graphQLFieldDefinition = getGraphQLFieldDefinition(queryAnnotation);
        builder = builder.field(graphQLFieldDefinition);
        MethodInfo methodInfo = queryAnnotation.target().asMethod();
        objectBag.getCodeRegistryBuilder().dataFetcher(
                FieldCoordinates.coordinates(annotationToScan.withoutPackagePrefix(),
                        graphQLFieldDefinition.getName()),
                new ReflectionDataFetcher(methodInfo, argumentsHelper.toArguments(methodInfo), annotationsForMethod,
                        objectBag));

        // return type
        if (!methodInfo.returnType().kind().equals(Type.Kind.VOID)) {
            scanType(methodInfo.returnType(), objectBag.getTypeMap(), this.outputTypeCreator);
        } else {
            throw new VoidReturnNotAllowedException("Can not have a void return for [" + annotationToScan.withoutPackagePrefix()
                    + "] on method [" + methodInfo.name() + "]");
        }

        // arguments on getters and setter
        List<Type> parameters = methodInfo.parameters();
        for (Type parameter : parameters) {
            scanType(parameter, objectBag.getInputMap(), this.inputTypeCreator);
        }

        return builder;
    }

    private GraphQLSchema createGraphQLSchema(GraphQLObjectType query, GraphQLObjectType mutation) {

        GraphQLSchema.Builder schemaBuilder = GraphQLSchema.newSchema();

        Set<GraphQLType> additionalTypes = new HashSet<>();
        additionalTypes.addAll(objectBag.getEnumMap().values());
        additionalTypes.addAll(objectBag.getTypeMap().values());
        additionalTypes.addAll(objectBag.getInputMap().values());
        additionalTypes.addAll(objectBag.getInterfaceMap().values());
        schemaBuilder = schemaBuilder.additionalTypes(additionalTypes);

        if (!query.getFieldDefinitions().isEmpty()) {
            schemaBuilder = schemaBuilder.query(query);
        }
        if (!mutation.getFieldDefinitions().isEmpty()) {
            schemaBuilder = schemaBuilder.mutation(mutation);
        }

        schemaBuilder = schemaBuilder.codeRegistry(objectBag.getCodeRegistryBuilder().build());

        return schemaBuilder.build();
    }

    private GraphQLFieldDefinition getGraphQLFieldDefinition(AnnotationInstance graphQLAnnotation) {
        MethodInfo methodInfo = graphQLAnnotation.target().asMethod();

        // Annotations on this method
        Annotations annotations = annotationsHelper.getAnnotationsForMethod(methodInfo);

        Type returnType = methodInfo.returnType();

        // Fields
        GraphQLFieldDefinition.Builder builder = GraphQLFieldDefinition.newFieldDefinition();

        // Name
        String fieldName = nameHelper.getExecutionTypeName(graphQLAnnotation, annotations);
        builder = builder.name(fieldName);

        // Type (output)
        builder = builder
                .type(outputTypeCreator.createGraphQLOutputType(returnType, annotations));

        // Arguments (input)
        builder.arguments(argumentsHelper.toGraphQLArguments(inputTypeCreator, methodInfo));

        // Description
        Optional<String> maybeDescription = descriptionHelper.getDescriptionForType(annotations);
        builder = builder.description(maybeDescription.orElse(null));

        return builder.build();
    }

    private <T extends GraphQLType> void scanType(Type type, Map<DotName, T> map, Creator creator) {
        switch (type.kind()) {
            case ARRAY:
                Type typeInArray = type.asArrayType().component();
                scanType(typeInArray, map, creator);
                break;
            case PARAMETERIZED_TYPE:
                Type typeInCollection = type.asParameterizedType().arguments().get(0);
                scanType(typeInCollection, map, creator);
                break;
            case PRIMITIVE:
                if (!objectBag.getScalarMap().containsKey(type.name())) {
                    LOG.warn("No scalar mapping for " + type.name() + WITH_KIND + type.kind());
                }
                break;
            case CLASS:
                if (!objectBag.getScalarMap().containsKey(type.name())) {
                    ClassInfo classInfo = index.getClassByName(type.name());
                    if (classInfo != null) {
                        scanClass(classInfo, map, creator);
                    } else {
                        LOG.warn("Not indexed class " + type.name() + WITH_KIND + type.kind());
                    }
                }
                break;
            default:
                LOG.error("What should we do with field type of " + type.name() + WITH_KIND + type.kind());
                break;
        }
    }

    private <T extends GraphQLType> void scanClass(ClassInfo classInfo, Map<DotName, T> map, Creator creator) {

        if (Classes.isEnum(classInfo)) {
            scanEnum(classInfo);
        } else {

            if (!map.containsKey(classInfo.name())) {
                GraphQLType type = creator.create(classInfo);
                map.putIfAbsent(classInfo.name(), (T) type);
                scanFieldsAndMethods(classInfo, map, creator);
            }
        }
    }

    private void scanEnum(ClassInfo classInfo) {
        if (Classes.isEnum(classInfo) &&
                !objectBag.getEnumMap().containsKey(classInfo.name())) {
            GraphQLEnumType created = enumTypeCreator.create(classInfo);
            objectBag.getEnumMap().putIfAbsent(classInfo.name(), created);
        }
    }

    private <T extends GraphQLType> void scanFieldsAndMethods(ClassInfo classInfo, Map<DotName, T> map,
            Creator creator) {
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
            boolean isGetter = nameHelper.isGetter(methodName);
            boolean isSetter = nameHelper.isSetter(methodName);
            boolean isVoid = methodInfo.returnType().kind().equals(Type.Kind.VOID);
            if (isSetter || isGetter) {
                if (isVoid && isGetter) {
                    throw new VoidReturnNotAllowedException("Getter method [" + methodName + "] can not hava a void return");
                } else if (!isVoid) {
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

    private static final String WITH_KIND = " with kind ";
}
