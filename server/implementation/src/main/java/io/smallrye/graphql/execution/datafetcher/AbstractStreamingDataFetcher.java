package io.smallrye.graphql.execution.datafetcher;

import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import org.dataloader.BatchLoaderEnvironment;
import org.eclipse.microprofile.graphql.GraphQLException;

import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetchingEnvironment;
import io.smallrye.graphql.SmallRyeGraphQLServerMessages;
import io.smallrye.graphql.execution.context.SmallRyeContext;
import io.smallrye.graphql.execution.context.SmallRyeContextManager;
import io.smallrye.graphql.execution.datafetcher.helper.BatchLoaderHelper;
import io.smallrye.graphql.schema.model.Operation;
import io.smallrye.graphql.schema.model.Type;
import io.smallrye.graphql.transformation.AbstractDataFetcherException;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.infrastructure.Infrastructure;

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
            DataFetchingEnvironment dfe,
            DataFetcherResult.Builder<Object> resultBuilder,
            Object[] transformedArguments) throws Exception {
        SmallRyeContext context = SmallRyeContextManager.getSmallRyeContext(dfe);
        try {
            SmallRyeContextManager.setCurrentSmallRyeContext(context);
            Multi<?> multi = handleUserMethodCall(transformedArguments);

            return (O) multi
                    .onItem().transform((t) -> {
                        try {
                            Object resultFromTransform = fieldHelper.transformOrAdaptResponse(t, dfe);
                            resultBuilder.data(resultFromTransform);
                            return (O) resultBuilder.build();
                        } catch (AbstractDataFetcherException abstractDataFetcherException) {
                            //Arguments or result couldn't be transformed
                            abstractDataFetcherException.appendDataFetcherResult(resultBuilder, dfe);
                            eventEmitter.fireOnDataFetchError(dfe.getExecutionId().toString(), abstractDataFetcherException);
                            return (O) resultBuilder.build();
                        }
                    })
                    .onFailure().recoverWithItem(new Function<Throwable, O>() {
                        @Override
                        public O apply(Throwable throwable) {
                            eventEmitter.fireOnDataFetchError(dfe.getExecutionId().toString(), throwable);
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

        } finally {
            SmallRyeContextManager.clearCurrentSmallRyeContext();
        }
    }

    protected abstract Multi<?> handleUserMethodCall(Object[] transformedArguments) throws Exception;

    @Override
    @SuppressWarnings("unchecked")
    protected <O> O invokeFailure(DataFetcherResult.Builder<Object> resultBuilder) {
        return (O) Multi.createFrom()
                .item(resultBuilder::build);
    }

    @Override
    @SuppressWarnings("unchecked")
    public CompletionStage<List<T>> load(List<K> keys, BatchLoaderEnvironment ble) {
        try {
            BatchLoaderHelper.ArgumentsAndContext argumentsAndContext = batchLoaderHelper.getArgumentsAndContext(keys, ble);
            SmallRyeContextManager.setCurrentSmallRyeContext(argumentsAndContext.smallRyeContext);
            return handleUserBatchLoad(argumentsAndContext.arguments)
                    .toUni().runSubscriptionOn(Infrastructure.getDefaultExecutor())
                    .subscribe().asCompletionStage();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        } finally {
            SmallRyeContextManager.clearCurrentSmallRyeContext();
        }
    }

    protected abstract Multi<List<T>> handleUserBatchLoad(final Object[] arguments) throws Exception;
}