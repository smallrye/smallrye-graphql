package io.smallrye.graphql.spi.datafetcher;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.dataloader.BatchLoaderEnvironment;

import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetchingEnvironment;
import io.smallrye.graphql.spi.DataFetcherService;
import io.smallrye.graphql.transformation.AbstractDataFetcherException;

/**
 * The default, built in data fetcher service (when none is specified)
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class DefaultDataFetcherService extends AbstractDataFetcherService {

    @Override
    public DataFetcherService newInstance() {
        return new DefaultDataFetcherService();
    }

    @Override
    public String getName() {
        return "Reflection (default)";
    }

    @Override
    public String[] forClasses() {
        return null; // This is the default, so all non-specified classes
    }

    @Override
    public <T> T invokeAndTransform(DataFetchingEnvironment dfe, DataFetcherResult.Builder<Object> resultBuilder,
            Object[] transformedArguments) throws AbstractDataFetcherException, Exception {
        Object resultFromMethodCall = reflectionHelper.invoke(transformedArguments);
        Object resultFromTransform = fieldHelper.transformResponse(resultFromMethodCall);
        resultBuilder.data(resultFromTransform);
        return (T) resultBuilder.build();
    }

    @Override
    public <T> T invokeFailure(DataFetcherResult.Builder<Object> resultBuilder) {
        return (T) resultBuilder.build();
    }

    @Override
    public <T> CompletionStage<List<T>> batch(BatchLoaderEnvironment ble, List<Object> keys) {
        Object[] arguments = batchLoaderHelper.getArguments(keys, ble);
        final ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        return CompletableFuture.supplyAsync(() -> (List<T>) reflectionHelper.invokePrivileged(tccl, arguments));
    }

}
