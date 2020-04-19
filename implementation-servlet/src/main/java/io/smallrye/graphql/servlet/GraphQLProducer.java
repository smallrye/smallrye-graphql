package io.smallrye.graphql.servlet;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import graphql.schema.GraphQLSchema;
import io.smallrye.graphql.bootstrap.Bootstrap;
import io.smallrye.graphql.execution.ExecutionService;
import io.smallrye.graphql.schema.model.Schema;

/**
 * Produces the GraphQL Schema
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@ApplicationScoped
public class GraphQLProducer {

    private GraphQLSchema graphQLSchema;
    private ExecutionService executionService;

    @Inject
    private GraphQLConfig config;

    public void initializeGraphQL(GraphQLConfig config, Schema schema) {
        this.graphQLSchema = Bootstrap.bootstrap(schema);
        this.executionService = new ExecutionService(config, graphQLSchema);
    }

    @Produces
    public GraphQLSchema getGraphQLSchema() {
        return this.graphQLSchema;
    }

    @Produces
    public ExecutionService getExecutionService() {
        return executionService;
    }
}
