package io.smallrye.graphql.execution.datafetcher;

import java.util.List;

import org.reactivestreams.Publisher;

import io.smallrye.graphql.schema.model.Operation;
import io.smallrye.mutiny.Multi;

/**
 * Handle Stream calls with Publisher
 *
 * @param <K>
 * @param <T>
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class PublisherDataFetcher<K, T> extends AbstractStreamingDataFetcher<K, T> {

    public PublisherDataFetcher(Operation operation) {
        super(operation);
    }

    @Override
    protected Multi<?> handleUserMethodCall(Object[] transformedArguments) throws Exception {
        Publisher<?> publisher = operationInvoker.invoke(transformedArguments);
        return (Multi<?>) Multi.createFrom().publisher(publisher);
    }

    @Override
    protected Multi<List<T>> handleUserBatchLoad(Object[] arguments) throws Exception {
        Publisher<List<T>> publisher = operationInvoker.invoke(arguments);
        return (Multi<List<T>>) Multi.createFrom().publisher(publisher);
    }
}
