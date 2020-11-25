package io.smallrye.graphql.spi.datafetcher;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.dataloader.BatchLoaderEnvironment;

import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetchingEnvironment;
import io.smallrye.graphql.schema.model.Field;
import io.smallrye.graphql.schema.model.Wrapper;
import io.smallrye.graphql.spi.WrapperHandlerService;
import io.smallrye.graphql.transformation.AbstractDataFetcherException;

/**
 * The default, built in data fetcher service (when none is specified)
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class DefaultWrapperHandlerService extends AbstractWrapperHandlerService {

    @Override
    public WrapperHandlerService newInstance() {
        return new DefaultWrapperHandlerService();
    }

    @Override
    public String getName() {
        return "Reflection (default)";
    }

    @Override
    public List<String> forClasses() {
        return null; // This is the default, so all non-specified classes
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
    public <T> CompletionStage<List<T>> getBatchData(BatchLoaderEnvironment ble, List<Object> keys) {
        Object[] arguments = batchLoaderHelper.getArguments(keys, ble);
        final ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        return CompletableFuture.supplyAsync(() -> (List<T>) reflectionHelper.invokePrivileged(tccl, arguments));
    }

    @Override
    public Wrapper unwrap(Field field, boolean isBatch) {
        if (isBatch) {
            // Batch (by default) is a collection,
            return field.getWrapper().getWrapper();
        }

        if (field.hasWrapper()) {
            if (field.getWrapper().isCollectionOrArray()) {
                return field.getWrapper();
            } else {
                // TODO: Move Generics logic here ?
            }
        }
        return null;
    }

}
