package io.smallrye.graphql.cdi.producer;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import graphql.schema.GraphQLSchema;
import io.smallrye.graphql.bootstrap.Bootstrap;
import io.smallrye.graphql.execution.ExecutionService;
import io.smallrye.graphql.schema.model.Schema;

/**
 * Produces the GraphQL Services
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@ApplicationScoped
public class GraphQLProducer {

    public void setSchema(Schema schema) {
        this.schema = schema;
    }

    public GraphQLSchema initialize(Schema schema) {
        return initialize(schema, false);
    }

    public GraphQLSchema initialize(Schema schema, boolean allowMultipleDeployments) {
        this.schema = schema;
        return initialize(allowMultipleDeployments);
    }

    public GraphQLSchema initialize(boolean allowMultipleDeployments) {

        this.graphQLSchema = Bootstrap.bootstrap(schema, allowMultipleDeployments);
        this.executionService = new ExecutionService(graphQLSchema, this.schema.getBatchOperations(),
                schema.hasSubscriptions());
        return this.graphQLSchema;
    }

    public GraphQLSchema initialize() {
        return initialize(false);
    }

    @Produces
    ExecutionService executionService;

    @Produces
    GraphQLSchema graphQLSchema;

    @Produces
    Schema schema;
}
