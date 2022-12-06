package io.smallrye.graphql.execution.datafetcher;

import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import org.eclipse.microprofile.graphql.GraphQLException;

import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetchingEnvironment;
import io.smallrye.graphql.SmallRyeGraphQLServerMessages;
import io.smallrye.graphql.api.Context;
import io.smallrye.graphql.schema.model.Operation;
import io.smallrye.graphql.schema.model.Type;
import io.smallrye.graphql.transformation.AbstractDataFetcherException;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import mutiny.zero.flow.adapters.AdaptersToReactiveStreams;

/**
 * Handle Stream calls with Multi, used as base for Multi and Publisher
 *
 * @param <K>
 * @param <T>
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public abstract class AbstractStreamingDataFetcher<K, T> extends AbstractDataFetcher<K, T> {

    public AbstractStreamingDataFetcher(Operation operation, Type type) {
        super(operation, type);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <O> O invokeAndTransform(
            Context context,
            DataFetchingEnvironment dfe,
            DataFetcherResult.Builder<Object> resultBuilder,
            Object[] transformedArguments) throws Exception {

        Multi<?> multi = handleUserMethodCall(dfe, transformedArguments)
                .onItem().transform((t) -> {
                    try {
                        Object resultFromTransform = fieldHelper.transformOrAdaptResponse(t, dfe);
                        resultBuilder.data(resultFromTransform);
                        return (O) resultBuilder.build();
                    } catch (AbstractDataFetcherException abstractDataFetcherException) {
                        //Arguments or result couldn't be transformed
                        abstractDataFetcherException.appendDataFetcherResult(resultBuilder, dfe);
                        eventEmitter.fireOnDataFetchError(context, abstractDataFetcherException);
                        return (O) resultBuilder.build();
                    }
                })
                .onFailure().recoverWithItem(new Function<Throwable, O>() {
                    @Override
                    public O apply(Throwable throwable) {
                        eventEmitter.fireOnDataFetchError(context, throwable);
                        if (throwable instanceof GraphQLException) {
                            GraphQLException graphQLException = (GraphQLException) throwable;
                            errorResultHelper.appendPartialResult(resultBuilder, dfe, graphQLException);
                        } else if (throwable instanceof Exception) {
                            DataFetcherException dataFetcherException = SmallRyeGraphQLServerMessages.msg
                                    .dataFetcherException(operation, throwable);
                            errorResultHelper.appendException(resultBuilder, dfe, dataFetcherException);
                        } else if (throwable instanceof Error) {
                            errorResultHelper.appendException(resultBuilder, dfe, throwable);
                        }
                        return (O) resultBuilder.build();
                    }
                });

        return (O) AdaptersToReactiveStreams.publisher(multi);

    }

    protected abstract Multi<?> handleUserMethodCall(DataFetchingEnvironment dfe, final Object[] transformedArguments)
            throws Exception;

    @Override
    @SuppressWarnings("unchecked")
    protected <O> O invokeFailure(DataFetcherResult.Builder<Object> resultBuilder) {
        return (O) Multi.createFrom()
                .item(resultBuilder::build);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected CompletionStage<List<T>> invokeBatch(DataFetchingEnvironment dfe, Object[] arguments) {
        try {
            return handleUserBatchLoad(dfe, arguments)
                    .toUni().runSubscriptionOn(Infrastructure.getDefaultExecutor())
                    .subscribe().asCompletionStage();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    protected abstract Multi<List<T>> handleUserBatchLoad(DataFetchingEnvironment dfe, final Object[] arguments)
            throws Exception;
}
