package io.smallrye.graphql.execution.datafetcher;

import java.util.List;
import java.util.concurrent.CompletionStage;

import org.dataloader.BatchLoaderEnvironment;

import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetchingEnvironment;
import io.smallrye.graphql.bootstrap.Config;
import io.smallrye.graphql.schema.model.Operation;
import io.smallrye.graphql.spi.ContextPropagationService;
import io.smallrye.graphql.transformation.AbstractDataFetcherException;

/**
 * The default, built in data fetcher
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @param <K>
 * @param <T>
 */
public class DefaultDataFetcher<K, T> extends AbstractDataFetcher<K, T> {

    public DefaultDataFetcher(Operation operation, Config config) {
        super(operation, config);
    }

    @Override
    public <T> T invokeAndTransform(DataFetchingEnvironment dfe, DataFetcherResult.Builder<Object> resultBuilder,
            Object[] transformedArguments) throws AbstractDataFetcherException, Exception {
        try {
            Object resultFromMethodCall = reflectionHelper.invoke(transformedArguments);
            Object resultFromTransform = fieldHelper.transformResponse(resultFromMethodCall);
            resultBuilder.data(resultFromTransform);
            return (T) resultBuilder.build();
        } catch (Exception e) {
            throw (Exception) unwrapThrowable(e);
        }
    }

    @Override
    public <T> T invokeFailure(DataFetcherResult.Builder<Object> resultBuilder) {
        return (T) resultBuilder.build();
    }

    @Override
    public CompletionStage<List<T>> load(List<K> keys, BatchLoaderEnvironment ble) {
        Object[] arguments = batchLoaderHelper.getArguments(keys, ble);
        final ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        ContextPropagationService contextPropagationService = ContextPropagationService.get();
        return contextPropagationService.withContextCapture(
                () -> (List<T>) reflectionHelper.invokePrivileged(tccl, arguments));
    }
}
