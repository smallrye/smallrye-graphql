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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.PostConstruct;
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
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLType;
import io.smallrye.graphql.execution.ReflectionDataFetcher;
import io.smallrye.graphql.index.Annotations;
import io.smallrye.graphql.schema.helper.AnnotationsHelper;
import io.smallrye.graphql.schema.helper.ArgumentsHelper;
import io.smallrye.graphql.schema.helper.DescriptionHelper;
import io.smallrye.graphql.schema.helper.NameHelper;
import io.smallrye.graphql.schema.holder.AnnotationsHolder;
import io.smallrye.graphql.schema.type.OutputTypeCreator;

/**
 * Creates the GraphQL Schema
 * TODO: Check that class is annotated with GraphQLApi ?
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
    private ArgumentsHelper argumentsHelper;

    @Inject
    private NameHelper nameHelper;

    @Inject
    private DescriptionHelper descriptionHelper;

    @Inject
    private GraphQLCodeRegistry.Builder codeRegistryBuilder;

    private GraphQLSchema graphQLSchema;

    @PostConstruct
    public void init() {
        graphQLSchema = createGraphQLSchema();
    }

    public GraphQLSchema getGraphQLSchema() {
        return graphQLSchema;
    }

    private GraphQLSchema createGraphQLSchema() {
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

    // TODO: Only scan @GraphQLAPI

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
                    builder = builder.description(maybeDescription.orElse(null));

                    // Type (output)
                    builder = builder
                            .type(outputTypeCreator.createGraphQLOutputType(returnType, annotations));

                    // Arguments (input)
                    builder.arguments(argumentsHelper.toGraphQLArguments(methodInfo, annotations));

                    GraphQLFieldDefinition graphQLFieldDefinition = builder.build();

                    queryTypeBuilder = queryTypeBuilder.field(graphQLFieldDefinition);

                    codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(name, graphQLFieldDefinition.getName()),
                            new ReflectionDataFetcher(methodInfo, argumentsHelper.toArgumentHolders(methodInfo)));
                    //        new LambdaMetafactoryDataFetcher(methodInfo));
                    //                    PropertyDataFetcher.fetching(methodInfo.name()));

                    break;
            }
        }

        return queryTypeBuilder.build();
    }

    private static final String QUERY = "Query";
    private static final String MUTATION = "Mutation";

}
