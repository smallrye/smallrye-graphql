package io.smallrye.graphql.spi;

import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import io.smallrye.graphql.api.Context;
import io.smallrye.graphql.execution.event.InvokeInfo;
import io.smallrye.graphql.schema.model.Operation;

/**
 * Some events during bootstrap and execution that allows extension
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public interface EventingService {

    // Config key
    public String getConfigKey();

    // Schema bootstrap
    default GraphQLSchema.Builder beforeSchemaBuild(GraphQLSchema.Builder builder) {
        return builder;
    }

    default Operation createOperation(Operation operation) {
        return operation;
    }

    // Execution

    default GraphQL.Builder beforeGraphQLBuild(GraphQL.Builder builder) {
        return builder;
    }

    default void beforeExecute(Context context) {
    }

    default void afterExecute(Context context) {
    }

    default void errorExecute(String executionId, Throwable t) {
    }

    default void beforeDataFetch(Context context) {
    }

    default void beforeInvoke(InvokeInfo invokeInfo) throws Exception {
    }

    default void afterDataFetch(Context context) {
    }

    default void errorDataFetch(String executionId, Throwable t) {
    }
}
