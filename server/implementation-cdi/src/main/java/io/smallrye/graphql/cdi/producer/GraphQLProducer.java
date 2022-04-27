package io.smallrye.graphql.cdi.producer;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;

import graphql.execution.ExecutionStrategy;
import graphql.schema.GraphQLSchema;
import io.smallrye.graphql.bootstrap.Bootstrap;
import io.smallrye.graphql.cdi.context.CDISmallRyeContext;
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
        return initialize(schema, null, null);
    }

    public GraphQLSchema initialize(Schema schema, ExecutionStrategy queryExecutionStrategy,
            ExecutionStrategy mutationExecutionStrategy) {
        return initialize(schema, false, queryExecutionStrategy, mutationExecutionStrategy);
    }

    public GraphQLSchema initialize(Schema schema, boolean allowMultipleDeployments) {
        return initialize(schema, allowMultipleDeployments, null, null);
    }

    public GraphQLSchema initialize(Schema schema, boolean allowMultipleDeployments, ExecutionStrategy queryExecutionStrategy,
            ExecutionStrategy mutationExecutionStrategy) {
        this.schema = schema;
        return initialize(allowMultipleDeployments, queryExecutionStrategy, mutationExecutionStrategy);
    }

    public GraphQLSchema initialize(boolean allowMultipleDeployments) {
        return initialize(allowMultipleDeployments, null, null);
    }

    public GraphQLSchema initialize(boolean allowMultipleDeployments, ExecutionStrategy queryExecutionStrategy,
            ExecutionStrategy mutationExecutionStrategy) {

        this.graphQLSchema = Bootstrap.bootstrap(schema, allowMultipleDeployments);
        this.executionService = new ExecutionService(graphQLSchema, this.schema, queryExecutionStrategy,
                mutationExecutionStrategy);
        return this.graphQLSchema;
    }

    public GraphQLSchema initialize(ExecutionStrategy queryExecutionStrategy, ExecutionStrategy mutationExecutionStrategy) {
        return initialize(false, queryExecutionStrategy, mutationExecutionStrategy);
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

    @Produces
    @Dependent
    public CDISmallRyeContext produceSmallRyeContext() {
        return new CDISmallRyeContext(GraphQLProducer.class.getName());
    }
}
