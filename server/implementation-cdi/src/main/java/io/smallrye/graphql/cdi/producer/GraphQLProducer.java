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
        this.schema = schema;
        return initialize();
    }

    public GraphQLSchema initialize() {

        this.graphQLSchema = Bootstrap.bootstrap(schema);
        this.executionService = new ExecutionService(graphQLSchema, this.schema.getBatchOperations(),
                schema.hasSubscriptions());
        return this.graphQLSchema;
    }

    @Produces
    ExecutionService executionService;

    @Produces
    GraphQLSchema graphQLSchema;

    @Produces
    Schema schema;
}
