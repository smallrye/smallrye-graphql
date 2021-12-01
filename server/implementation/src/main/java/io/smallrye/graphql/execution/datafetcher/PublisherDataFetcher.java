package io.smallrye.graphql.execution.datafetcher;

import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import org.dataloader.BatchLoaderEnvironment;
import org.eclipse.microprofile.graphql.GraphQLException;
import org.reactivestreams.Publisher;

import graphql.GraphQLContext;
import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetchingEnvironment;
import io.smallrye.graphql.SmallRyeGraphQLServerMessages;
import io.smallrye.graphql.execution.context.SmallRyeContext;
import io.smallrye.graphql.schema.model.Operation;
import io.smallrye.graphql.transformation.AbstractDataFetcherException;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;

/**
 * Handle Stream calls with Publisher
 *
 * @param <K>
 * @param <T>
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class PublisherDataFetcher<K, T> extends AbstractDataFetcher<K, T> {

    public PublisherDataFetcher(Operation operation) {
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
            Publisher<?> publisher = operationInvoker.invoke(transformedArguments);

            Multi<?> multi = Multi.createFrom().publisher(publisher);

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

        Publisher<List<T>> publisher = (Publisher<List<T>>) operationInvoker.invokePrivileged(tccl, arguments);

        return Multi.createFrom().publisher(publisher).toUni().runSubscriptionOn(Infrastructure.getDefaultExecutor())
                .subscribe().asCompletionStage();
    }
}
