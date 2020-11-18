package io.smallrye.graphql.spi.datafetcher;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

import org.dataloader.BatchLoaderEnvironment;
import org.eclipse.microprofile.graphql.GraphQLException;

import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetchingEnvironment;
import io.smallrye.graphql.SmallRyeGraphQLServerMessages;
import io.smallrye.graphql.schema.model.Field;
import io.smallrye.graphql.schema.model.Wrapper;
import io.smallrye.graphql.spi.WrapperHandlerService;
import io.smallrye.graphql.transformation.AbstractDataFetcherException;

/**
 * Handle Async calls with CompletableFuture
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class CompletableFutureWrapperHandlerService extends AbstractWrapperHandlerService {

    private static final List<String> FOR_CLASSES = Arrays.asList(new String[] {
            CompletableFuture.class.getName(),
            CompletionStage.class.getName()
    });

    @Override
    public String getName() {
        return "CompletableFuture (Built-in)";
    }

    @Override
    public List<String> forClasses() {
        return FOR_CLASSES;
    }

    @Override
    public WrapperHandlerService newInstance() {
        return new CompletableFutureWrapperHandlerService();
    }

    @Override
    protected <T> T invokeAndTransform(DataFetchingEnvironment dfe, DataFetcherResult.Builder<Object> resultBuilder,
            Object[] transformedArguments) throws AbstractDataFetcherException, Exception {
        CompletionStage<Object> futureResultFromMethodCall = reflectionHelper.invoke(transformedArguments);

        return (T) futureResultFromMethodCall.handle((result, throwable) -> {
            if (throwable instanceof CompletionException) {
                //Exception thrown by underlying method may be wrapped in CompletionException
                throwable = throwable.getCause();
            }

            if (throwable != null) {
                eventEmitter.fireOnDataFetchError(dfe.getExecutionId().toString(), throwable);
                if (throwable instanceof GraphQLException) {
                    GraphQLException graphQLException = (GraphQLException) throwable;
                    partialResultHelper.appendPartialResult(resultBuilder, dfe, graphQLException);
                } else if (throwable instanceof Exception) {
                    throw SmallRyeGraphQLServerMessages.msg.dataFetcherException(operation, throwable);
                } else if (throwable instanceof Error) {
                    throw ((Error) throwable);
                }
            } else {
                try {
                    resultBuilder.data(fieldHelper.transformResponse(result));
                } catch (AbstractDataFetcherException te) {
                    te.appendDataFetcherResult(resultBuilder, dfe);
                }
            }

            return resultBuilder.build();
        });
    }

    @Override
    protected <T> T invokeFailure(DataFetcherResult.Builder<Object> resultBuilder) {
        return (T) CompletableFuture.completedFuture(resultBuilder.build());
    }

    @Override
    public <T> CompletionStage<List<T>> getBatchData(BatchLoaderEnvironment ble, List<Object> keys) {
        Object[] arguments = batchLoaderHelper.getArguments(keys, ble);
        final ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        return (CompletableFuture<List<T>>) reflectionHelper.invokePrivileged(tccl, arguments);
    }

    @Override
    public Wrapper unwrap(Field field, boolean isBatch) {
        if (field.hasWrapper()) {
            if (FOR_CLASSES.contains(field.getWrapper().getWrapperClassName())) {
                if (isBatch) {
                    return field.getWrapper().getWrapper().getWrapper();
                } else {
                    return field.getWrapper().getWrapper();
                }
            } else {
                if (isBatch) {
                    return field.getWrapper().getWrapper();
                } else {
                    return field.getWrapper();
                }
            }
        }

        return null;
    }

}
