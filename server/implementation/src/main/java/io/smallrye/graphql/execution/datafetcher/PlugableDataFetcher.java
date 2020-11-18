package io.smallrye.graphql.execution.datafetcher;

import graphql.GraphQLContext;
import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import io.smallrye.graphql.bootstrap.Config;
import io.smallrye.graphql.execution.context.SmallRyeContext;
import io.smallrye.graphql.schema.model.Operation;
import io.smallrye.graphql.spi.WrapperHandlerService;

/**
 * Delegate the actual fetching to a SPI implementation.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class PlugableDataFetcher<T> implements DataFetcher<T> {
    private final WrapperHandlerService dataFetcherService;
    private final Operation operation;

    public PlugableDataFetcher(Operation operation, Config config) {
        this.operation = operation;
        this.dataFetcherService = WrapperHandlerService.getWrapperHandlerService(operation);
        this.dataFetcherService.initDataFetcher(operation, config);
    }

    @Override
    public T get(final DataFetchingEnvironment dfe) throws Exception {
        SmallRyeContext.setDataFromFetcher(dfe, operation);

        final GraphQLContext context = dfe.getContext();
        final DataFetcherResult.Builder<Object> resultBuilder = DataFetcherResult.newResult().localContext(context);

        return (T) dataFetcherService.getData(dfe, resultBuilder);
    }
}
