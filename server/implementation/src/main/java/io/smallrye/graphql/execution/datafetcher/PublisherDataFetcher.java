package io.smallrye.graphql.execution.datafetcher;

import java.util.List;
import java.util.concurrent.CompletionStage;

import org.dataloader.BatchLoaderEnvironment;
import org.reactivestreams.Publisher;

import graphql.GraphQLContext;
import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetchingEnvironment;
import io.smallrye.graphql.bootstrap.Config;
import io.smallrye.graphql.execution.context.SmallRyeContext;
import io.smallrye.graphql.schema.model.Operation;
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

    public PublisherDataFetcher(Operation operation, Config config) {
        super(operation, config);
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
            Publisher<?> publisher = reflectionHelper.invoke(transformedArguments);
            return (O) publisher;
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

        Publisher<List<T>> publisher = (Publisher<List<T>>) reflectionHelper.invokePrivileged(tccl, arguments);

        return Multi.createFrom().publisher(publisher).toUni().runSubscriptionOn(Infrastructure.getDefaultExecutor())
                .subscribe().asCompletionStage();
    }
}
