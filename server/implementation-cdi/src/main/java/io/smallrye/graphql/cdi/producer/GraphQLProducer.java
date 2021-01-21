package io.smallrye.graphql.cdi.producer;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import graphql.schema.GraphQLSchema;
import io.smallrye.graphql.api.Context;
import io.smallrye.graphql.bootstrap.Bootstrap;
import io.smallrye.graphql.cdi.config.GraphQLConfig;
import io.smallrye.graphql.execution.ExecutionService;
import io.smallrye.graphql.execution.SchemaPrinter;
import io.smallrye.graphql.execution.context.SmallRyeContext;
import io.smallrye.graphql.schema.model.Schema;

/**
 * Produces the GraphQL Services
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@ApplicationScoped
public class GraphQLProducer {

    @Inject
    GraphQLConfig graphQLConfig;

    public void setSchema(Schema schema) {
        this.schema = schema;
    }

    public GraphQLSchema initialize(Schema schema) {
        this.schema = schema;
        return initialize();
    }

    public GraphQLSchema initialize(Schema schema, GraphQLConfig graphQLConfig) {
        this.schema = schema;
        this.graphQLConfig = graphQLConfig;
        return initialize();
    }

    public GraphQLSchema initialize() {
        this.graphQLSchema = Bootstrap.bootstrap(schema, graphQLConfig);

        this.executionService = new ExecutionService(graphQLConfig, graphQLSchema, this.schema.getBatchOperations());
        this.schemaPrinter = new SchemaPrinter(graphQLConfig);
        return this.graphQLSchema;
    }

    @Produces
    @Dependent
    public Context getContext() {
        return SmallRyeContext.getContext();
    }

    @Produces
    SchemaPrinter schemaPrinter;

    @Produces
    ExecutionService executionService;

    @Produces
    GraphQLSchema graphQLSchema;

    @Produces
    Schema schema;
}
