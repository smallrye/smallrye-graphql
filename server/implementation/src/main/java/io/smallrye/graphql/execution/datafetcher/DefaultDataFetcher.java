package io.smallrye.graphql.execution.datafetcher;

import static io.smallrye.graphql.SmallRyeGraphQLServerMessages.msg;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.dataloader.BatchLoaderEnvironment;
import org.eclipse.microprofile.context.ThreadContext;

import graphql.GraphQLContext;
import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetchingEnvironment;
import io.smallrye.graphql.execution.context.SmallRyeContext;
import io.smallrye.graphql.schema.model.Operation;

/**
 * The default, built in data fetcher
 *
 * @param <K>
 * @param <T>
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class DefaultDataFetcher<K, T> extends AbstractDataFetcher<K, T> {

    public DefaultDataFetcher(Operation operation) {
        super(operation);
    }

    @Override
    public <T> T invokeAndTransform(DataFetchingEnvironment dfe, DataFetcherResult.Builder<Object> resultBuilder,
            Object[] transformedArguments) throws Exception {
        SmallRyeContext context = ((GraphQLContext) dfe.getContext()).get("context");
        try {
            SmallRyeContext.setContext(context);
            Object resultFromMethodCall = operationInvoker.invoke(transformedArguments);
            Object resultFromTransform = fieldHelper.transformOrAdaptResponse(resultFromMethodCall);
            resultBuilder.data(resultFromTransform);
            return (T) resultBuilder.build();
        } finally {
            SmallRyeContext.remove();
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
        final SmallRyeContext context = ble.getContext();

        ThreadContext threadContext = ThreadContext.builder().build();
        try {
            SmallRyeContext.setContext(context);

            CompletableFuture<List<T>> reflectionSupplier = CompletableFuture.supplyAsync(() -> {
                try {
                    return (List<T>) operationInvoker.invokePrivileged(tccl, arguments);
                } catch (Exception e) {
                    if (e instanceof RuntimeException && e.getCause() != null && !(e.getCause() instanceof RuntimeException)) {
                        throw msg.dataFetcherException(operation, e.getCause());
                    } else if (e instanceof RuntimeException) {
                        throw (RuntimeException) e;
                    } else {
                        throw msg.dataFetcherException(operation, e);
                    }
                }
            }, threadContext.currentContextExecutor());

            return threadContext
                    .withContextCapture(reflectionSupplier);
        } finally {
            SmallRyeContext.remove();
        }
    }
}
