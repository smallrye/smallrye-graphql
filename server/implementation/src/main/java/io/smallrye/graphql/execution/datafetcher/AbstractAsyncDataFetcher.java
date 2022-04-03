package io.smallrye.graphql.execution.datafetcher;

import java.util.List;
import java.util.concurrent.CompletionStage;

import org.dataloader.BatchLoaderEnvironment;
import org.eclipse.microprofile.graphql.GraphQLException;

import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetchingEnvironment;
import io.smallrye.graphql.SmallRyeGraphQLServerMessages;
import io.smallrye.graphql.execution.context.SmallRyeContext;
import io.smallrye.graphql.execution.context.SmallRyeContextManager;
import io.smallrye.graphql.schema.model.Operation;
import io.smallrye.graphql.transformation.AbstractDataFetcherException;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;

/**
 * Handle Async calls with Uni, base for Uni and CompletionState Datafetchers
 *
 * @param <K>
 * @param <T>
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public abstract class AbstractAsyncDataFetcher<K, T> extends AbstractDataFetcher<K, T> {

    public AbstractAsyncDataFetcher(Operation operation) {
        super(operation);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <O> O invokeAndTransform(
            DataFetchingEnvironment dfe,
            DataFetcherResult.Builder<Object> resultBuilder,
            Object[] transformedArguments) throws Exception {

        SmallRyeContext smallRyeContext = SmallRyeContextManager.getCurrentSmallRyeContext();

        Uni<?> uni = handleUserMethodCall(transformedArguments);

        return (O) uni
                .onSubscription().invoke(() -> {
                    SmallRyeContextManager.setCurrentSmallRyeContext(smallRyeContext);
                })
                .onItemOrFailure()
                .transformToUni((result, throwable, emitter) -> {
                    if (throwable != null) {
                        eventEmitter.fireOnDataFetchError(dfe.getExecutionId().toString(), throwable);
                        if (throwable instanceof GraphQLException) {
                            GraphQLException graphQLException = (GraphQLException) throwable;
                            errorResultHelper.appendPartialResult(resultBuilder, dfe, graphQLException);
                        } else if (throwable instanceof Exception) {
                            emitter.fail(SmallRyeGraphQLServerMessages.msg.dataFetcherException(operation, throwable));
                            return;
                        } else if (throwable instanceof Error) {
                            emitter.fail(throwable);
                            return;
                        }
                    } else {
                        try {
                            resultBuilder.data(fieldHelper.transformOrAdaptResponse(result, dfe));
                        } catch (AbstractDataFetcherException te) {
                            te.appendDataFetcherResult(resultBuilder, dfe);
                        }
                    }

                    emitter.complete(resultBuilder.build());
                })
                .runSubscriptionOn(Infrastructure.getDefaultExecutor())
                .subscribe()
                .asCompletionStage();
    }

    protected abstract Uni<?> handleUserMethodCall(Object[] transformedArguments)
            throws Exception;

    @Override
    @SuppressWarnings("unchecked")
    protected <O> O invokeFailure(DataFetcherResult.Builder<Object> resultBuilder) {
        return (O) Uni.createFrom()
                .item(resultBuilder::build)
                .runSubscriptionOn(Infrastructure.getDefaultExecutor())
                .subscribe()
                .asCompletionStage();
    }

    @Override
    @SuppressWarnings("unchecked")
    public CompletionStage<List<T>> load(List<K> keys, BatchLoaderEnvironment ble) {
        try {
            SmallRyeContext smallRyeContext = SmallRyeContextManager.getCurrentSmallRyeContext();
            Object[] arguments = batchLoaderHelper.getArguments(keys, ble);
            return handleUserBatchLoad(arguments)
                    .onSubscription().invoke(() -> {
                        SmallRyeContextManager.setCurrentSmallRyeContext(smallRyeContext);
                    })
                    .runSubscriptionOn(Infrastructure.getDefaultExecutor())
                    .subscribe().asCompletionStage();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    protected abstract Uni<List<T>> handleUserBatchLoad(final Object[] arguments) throws Exception;
}