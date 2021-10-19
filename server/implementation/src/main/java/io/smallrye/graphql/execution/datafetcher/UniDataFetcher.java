package io.smallrye.graphql.execution.datafetcher;

import java.util.List;
import java.util.concurrent.CompletionStage;

import org.dataloader.BatchLoaderEnvironment;
import org.eclipse.microprofile.graphql.GraphQLException;

import graphql.GraphQLContext;
import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetchingEnvironment;
import io.smallrye.graphql.SmallRyeGraphQLServerMessages;
import io.smallrye.graphql.execution.context.SmallRyeContext;
import io.smallrye.graphql.schema.model.Operation;
import io.smallrye.graphql.transformation.AbstractDataFetcherException;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;

/**
 * Handle Async calls with Uni
 *
 * @param <K>
 * @param <T>
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class UniDataFetcher<K, T> extends AbstractDataFetcher<K, T> {

    public UniDataFetcher(Operation operation) {
        super(operation);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <O> O invokeAndTransform(
            DataFetchingEnvironment dfe,
            DataFetcherResult.Builder<Object> resultBuilder,
            Object[] transformedArguments) throws Exception {
        SmallRyeContext context = ((GraphQLContext) dfe.getContext()).get("context");
        try {
            SmallRyeContext.setContext(context);
            Uni<?> uni = reflectionHelper.invoke(transformedArguments);
            return (O) uni
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
                                resultBuilder.data(fieldHelper.transformResponse(result));
                            } catch (AbstractDataFetcherException te) {
                                te.appendDataFetcherResult(resultBuilder, dfe);
                            }
                        }

                        emitter.complete(resultBuilder.build());
                    })
                    .runSubscriptionOn(Infrastructure.getDefaultExecutor())
                    .subscribe()
                    .asCompletionStage();
        } finally {
            SmallRyeContext.remove();
        }
    }

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
        Object[] arguments = batchLoaderHelper.getArguments(keys, ble);
        final ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        return ((Uni<List<T>>) reflectionHelper.invokePrivileged(tccl, arguments))
                .runSubscriptionOn(Infrastructure.getDefaultExecutor())
                .subscribe().asCompletionStage();
    }
}
