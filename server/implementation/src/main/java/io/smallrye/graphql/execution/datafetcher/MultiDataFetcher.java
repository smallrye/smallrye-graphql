package io.smallrye.graphql.execution.datafetcher;

import java.util.List;

import org.reactivestreams.Publisher;

import graphql.schema.DataFetchingEnvironment;
import io.smallrye.graphql.schema.model.Operation;
import io.smallrye.graphql.schema.model.Type;
import io.smallrye.mutiny.Multi;
import mutiny.zero.flow.adapters.AdaptersToFlow;

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
    protected Multi<?> handleUserMethodCall(DataFetchingEnvironment dfe, final Object[] transformedArguments)
            throws Exception {
        Publisher<?> rsPub = operationInvoker.invoke(transformedArguments);
        return (Multi<?>) AdaptersToFlow.publisher(rsPub);
    }

    @Override
    protected Multi<List<T>> handleUserBatchLoad(DataFetchingEnvironment dfe, final Object[] arguments) throws Exception {
        Publisher<?> rsPub = operationInvoker.invoke(arguments);
        return (Multi<List<T>>) AdaptersToFlow.publisher(rsPub);
    }
}
