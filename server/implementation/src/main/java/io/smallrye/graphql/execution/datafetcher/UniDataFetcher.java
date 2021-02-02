package io.smallrye.graphql.execution.datafetcher;

import java.util.List;
import java.util.concurrent.CompletionStage;

import org.dataloader.BatchLoaderEnvironment;
import org.eclipse.microprofile.graphql.GraphQLException;

import graphql.GraphQLContext;
import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetchingEnvironment;
import io.smallrye.graphql.SmallRyeGraphQLServerMessages;
import io.smallrye.graphql.bootstrap.Config;
import io.smallrye.graphql.execution.context.SmallRyeContext;
import io.smallrye.graphql.schema.model.Operation;
import io.smallrye.graphql.transformation.AbstractDataFetcherException;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;

/**
 * Handle Async calls with Uni
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @param <K>
 * @param <T>
 */
public class UniDataFetcher<K, T> extends AbstractDataFetcher<K, T> {

    public UniDataFetcher(Operation operation, Config config) {
        super(operation, config);
    }

    @Override
    protected <T> T invokeAndTransform(DataFetchingEnvironment dfe, DataFetcherResult.Builder<Object> resultBuilder,
            Object[] transformedArguments) throws AbstractDataFetcherException, Exception {
        SmallRyeContext context = ((GraphQLContext) dfe.getContext()).get("context");
        try {
            SmallRyeContext.setContext(context);
            Uni<?> uni = reflectionHelper.invoke(transformedArguments);
            return (T) uni
                    .onItemOrFailure().transform((result, throwable) -> {

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
                    }).runSubscriptionOn(Infrastructure.getDefaultExecutor()).subscribe().asCompletionStage();
        } finally {
            SmallRyeContext.remove();
        }
    }

    @Override
    protected <T> T invokeFailure(DataFetcherResult.Builder<Object> resultBuilder) {
        return (T) Uni.createFrom().item(() -> resultBuilder.build()).runSubscriptionOn(Infrastructure.getDefaultExecutor())
                .subscribe().asCompletionStage();
    }

    @Override
    public CompletionStage<List<T>> load(List<K> keys, BatchLoaderEnvironment ble) {
        Object[] arguments = batchLoaderHelper.getArguments(keys, ble);
        final ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        return ((Uni<List<T>>) reflectionHelper.invokePrivileged(tccl, arguments))
                .runSubscriptionOn(Infrastructure.getDefaultExecutor())
                .subscribe().asCompletionStage();
    }
}
