package io.smallrye.graphql.spi;

import java.util.ServiceLoader;

import graphql.schema.GraphQLSchema;
import io.smallrye.graphql.api.Context;
import io.smallrye.graphql.schema.model.Operation;

/**
 * Some events during bootstrap and execution that allows extension
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public interface EventingService {

    ServiceLoader<EventingService> eventingServices = ServiceLoader.load(EventingService.class);

    // Schema bootstrap
    default GraphQLSchema.Builder beforeSchemaBuild(GraphQLSchema.Builder builder) {
        return builder;
    }

    default Operation createOperation(Operation operation) {
        return operation;
    }

    // Execution

    default void beforeExecute(Context context) {
    }

    default void errorExecute(Context context) {
    }

    default void afterExecute(Context context) {
    }

    default void beforeDataFetch(Context context) {
    }

    default void errorDataFetch(Context context) {
    }

    default void afterDataFetch(Context context) {
    }
}
