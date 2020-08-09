package io.smallrye.graphql.spi;

import java.util.ServiceLoader;

import graphql.schema.GraphQLSchema;
import io.smallrye.graphql.api.Context;
import io.smallrye.graphql.schema.model.Operation;

/**
 * Some event during bootstrap and execution that allows extension
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
    default void beforeDataFetch(Context context) {

    }

    default void afterDataFetch(Context context) {

    }

}
