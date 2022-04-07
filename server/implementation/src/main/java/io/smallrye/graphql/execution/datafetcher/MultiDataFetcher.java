package io.smallrye.graphql.execution.datafetcher;

import java.util.List;

import io.smallrye.graphql.schema.model.Operation;
import io.smallrye.graphql.schema.model.Type;
import io.smallrye.mutiny.Multi;

/**
 * Handle Stream calls with Multi
 *
 * @param <K>
 * @param <T>
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class MultiDataFetcher<K, T> extends AbstractStreamingDataFetcher<K, T> {

    public MultiDataFetcher(Operation operation, Type type) {
        super(operation, type);
    }

    @Override
    protected Multi<?> handleUserMethodCall(Object[] transformedArguments) throws Exception {
        return (Multi<?>) operationInvoker.invoke(transformedArguments);
    }

    @Override
    protected Multi<List<T>> handleUserBatchLoad(Object[] arguments) throws Exception {
        return (Multi<List<T>>) operationInvoker.invoke(arguments);
    }
}
