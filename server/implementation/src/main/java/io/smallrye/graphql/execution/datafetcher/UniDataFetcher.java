package io.smallrye.graphql.execution.datafetcher;

import java.util.List;

import io.smallrye.graphql.schema.model.Operation;
import io.smallrye.mutiny.Uni;

/**
 * Handle Async calls with Uni
 *
 * @param <K>
 * @param <T>
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class UniDataFetcher<K, T> extends AbstractAsyncDataFetcher<K, T> {

    public UniDataFetcher(Operation operation) {
        super(operation);
    }

    @Override
    protected Uni<?> handleUserMethodCall(final Object[] transformedArguments)
            throws Exception {
        return (Uni<?>) operationInvoker.invoke(transformedArguments);
    }

    @Override
    public Uni<List<T>> handleUserBatchLoad(final Object[] arguments) throws Exception {
        return ((Uni<List<T>>) operationInvoker.invoke(arguments));
    }
}
