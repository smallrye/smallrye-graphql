package io.smallrye.graphql.execution.datafetcher;

import java.util.List;
import java.util.concurrent.Flow;

import org.reactivestreams.Publisher;

import graphql.schema.DataFetchingEnvironment;
import io.smallrye.graphql.schema.model.Operation;
import io.smallrye.graphql.schema.model.Type;
import io.smallrye.mutiny.Multi;
import mutiny.zero.flow.adapters.AdaptersToFlow;

/**
 * Handle Stream calls with Publisher
 *
 * @param <K>
 * @param <T>
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class PublisherDataFetcher<K, T> extends AbstractStreamingDataFetcher<K, T> {

    public PublisherDataFetcher(Operation operation, Type type) {
        super(operation, type);
    }

    @Override
    protected Multi<?> handleUserMethodCall(DataFetchingEnvironment dfe, final Object[] transformedArguments)
            throws Exception {
        return toMulti(operationInvoker.invoke(transformedArguments));
    }

    @Override
    protected Multi<List<T>> handleUserBatchLoad(DataFetchingEnvironment dfe, final Object[] arguments) throws Exception {
        return toMulti(operationInvoker.invoke(arguments));
    }

    @SuppressWarnings("unchecked")
    private <R> Multi<R> toMulti(Object result) {
        if (result == null) {
            throw new IllegalStateException(
                    "Subscription method " + operation.getName() + " returned null");
        }
        if (result instanceof Flow.Publisher) {
            return Multi.createFrom().publisher((Flow.Publisher<R>) result);
        }
        if (result instanceof Publisher) {
            return (Multi<R>) Multi.createFrom().publisher(AdaptersToFlow.publisher((Publisher<?>) result));
        }
        throw new IllegalStateException(
                "Subscription method " + operation.getName() + " returned " + result.getClass().getName()
                        + " which is neither Flow.Publisher nor org.reactivestreams.Publisher");
    }
}
