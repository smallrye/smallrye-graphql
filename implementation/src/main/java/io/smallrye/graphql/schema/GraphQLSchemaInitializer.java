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

package io.smallrye.graphql.schema;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;
import org.jboss.logging.Logger;

import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLType;
import io.smallrye.graphql.execution.ReflectionDataFetcher;
import io.smallrye.graphql.index.Annotations;
import io.smallrye.graphql.schema.helper.AnnotationsHelper;
import io.smallrye.graphql.schema.helper.DefaultValueHelper;
import io.smallrye.graphql.schema.helper.DescriptionHelper;
import io.smallrye.graphql.schema.helper.NameHelper;
import io.smallrye.graphql.schema.holder.AnnotationsHolder;
import io.smallrye.graphql.schema.type.InputTypeCreator;
import io.smallrye.graphql.schema.type.OutputTypeCreator;

/**
 * Creates the GraphQL Schema
 * TODO: Check that class is annotated with GraphQLApi ?
 * TODO: Check duplication with TypeMappingInitializer
 * TODO: Exceptions
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
    private Map<DotName, GraphQLInputObjectType> inputObjectMap;

    @Inject
    private Map<DotName, GraphQLObjectType> outputObjectMap;

    @Inject
    private Map<DotName, GraphQLEnumType> enumMap;

    @Inject
    @ConfigProperty(name = "mp.graphql.queryRootDescription", defaultValue = "Query root")
    private String queryRootDescription;

    @Inject
    @ConfigProperty(name = "mp.graphql.mutationRootDescription", defaultValue = "Mutation root")
    private String mutationRootDescription;

    @Inject
    private AnnotationsHelper annotationsHelper;

    @Inject
    private DefaultValueHelper defaultValueHelper;

    @Inject
    private NameHelper nameHelper;

    @Inject
    private DescriptionHelper descriptionHelper;

    @Inject
    private GraphQLCodeRegistry.Builder codeRegistryBuilder;

    public GraphQLSchema createGraphQLSchema() {
        GraphQLSchema.Builder schemaBuilder = GraphQLSchema.newSchema();

        Set<GraphQLType> additionalTypes = new HashSet<>();
        additionalTypes.addAll(enumMap.values());
        additionalTypes.addAll(outputObjectMap.values());
        additionalTypes.addAll(inputObjectMap.values());
        schemaBuilder.additionalTypes(additionalTypes);

        GraphQLObjectType allQueries = createGraphQLObjectType(Annotations.QUERY, QUERY, queryRootDescription);
        GraphQLObjectType allMutations = createGraphQLObjectType(Annotations.MUTATION, MUTATION, mutationRootDescription);
        schemaBuilder.query(allQueries);
        schemaBuilder.mutation(allMutations);

        schemaBuilder.codeRegistry(codeRegistryBuilder.build());

        return schemaBuilder.build();
    }

    private GraphQLObjectType createGraphQLObjectType(DotName annotationToScan, String name, String description) {
        List<AnnotationInstance> graphQLAnnotations = this.index.getAnnotations(annotationToScan);

        GraphQLObjectType.Builder queryTypeBuilder = GraphQLObjectType.newObject().name(name).description(description);
        // TODO: Take GraphQLApi into account
        for (AnnotationInstance graphQLAnnotation : graphQLAnnotations) {
            switch (graphQLAnnotation.target().kind()) {
                case METHOD:
                    MethodInfo methodInfo = graphQLAnnotation.target().asMethod();

                    // Annotations on this method
                    AnnotationsHolder annotations = annotationsHelper.getAnnotationsForMethod(methodInfo);

                    Type returnType = methodInfo.returnType();

                    // Fields
                    GraphQLFieldDefinition.Builder builder = GraphQLFieldDefinition.newFieldDefinition();
                    // Name // TODO: Need test cases for this
                    String fieldName = nameHelper.getExecutionTypeName(graphQLAnnotation, annotations);
                    builder = builder.name(fieldName);

                    // Description
                    Optional<String> maybeDescription = descriptionHelper.getDescription(annotations);
                    if (maybeDescription.isPresent()) {
                        builder = builder.description(maybeDescription.get());
                    }
                    // Type (output)
                    builder = builder
                            .type(outputTypeCreator.createGraphQLOutputType(returnType, annotations));

                    // Arguments (input)
                    builder.arguments(toGraphQLArguments(methodInfo, annotations));

                    queryTypeBuilder = queryTypeBuilder.field(builder.build());

                    codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(name, fieldName),
                            new ReflectionDataFetcher(methodInfo));
                    //        new LambdaMetafactoryDataFetcher(methodInfo));
                    //                    PropertyDataFetcher.fetching(methodInfo.name()));

                    break;
            }
        }

        return queryTypeBuilder.build();
    }

    private List<GraphQLArgument> toGraphQLArguments(MethodInfo methodInfo, AnnotationsHolder annotations) {
        List<Type> parameters = methodInfo.parameters();
        List<GraphQLArgument> r = new ArrayList<>();
        short cnt = 0;
        for (Type parameter : parameters) {
            r.add(toGraphQLArgument(methodInfo, cnt, parameter, annotations));
            cnt++;
        }
        return r;
    }

    private GraphQLArgument toGraphQLArgument(MethodInfo methodInfo, short argCount, Type parameter,
            AnnotationsHolder annotations) {
        AnnotationsHolder annotationsForThisArgument = annotationsHelper.getAnnotationsForArgument(methodInfo, argCount);

        String argName = nameHelper.getArgumentName(annotationsForThisArgument, argCount);
        GraphQLInputType inputType = inputTypeCreator.createGraphQLInputType(parameter, annotations);

        GraphQLArgument.Builder argumentBuilder = GraphQLArgument.newArgument();
        argumentBuilder = argumentBuilder.name(argName);
        argumentBuilder = argumentBuilder.type(inputType);
        Optional<Object> maybeDefaultValue = defaultValueHelper.getDefaultValue(annotationsForThisArgument);
        if (maybeDefaultValue.isPresent()) {
            argumentBuilder = argumentBuilder.defaultValue(maybeDefaultValue.get());
        }

        return argumentBuilder.build();
    }

    private static final String QUERY = "Query";
    private static final String MUTATION = "Mutation";

}
