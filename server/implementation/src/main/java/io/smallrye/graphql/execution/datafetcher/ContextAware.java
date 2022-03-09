package io.smallrye.graphql.execution.datafetcher;

import org.dataloader.BatchLoaderEnvironment;
import org.dataloader.DataLoader;

import graphql.GraphQLContext;
import graphql.schema.DataFetchingEnvironment;
import io.smallrye.graphql.execution.context.SmallRyeBatchLoaderContextProvider;
import io.smallrye.graphql.execution.context.SmallRyeContext;
import io.smallrye.graphql.schema.model.Field;

public interface ContextAware {
    static final String CONTEXT = "context";

    default SmallRyeContext getSmallRyeContext(final DataFetchingEnvironment dfe) {
        GraphQLContext graphQLContext = dfe.getGraphQlContext();
        return graphQLContext.get(CONTEXT);
    }

    default SmallRyeContext getSmallRyeContext(final BatchLoaderEnvironment ble) {
        GraphQLContext graphQLContext = ble.getContext();
        return graphQLContext.get(CONTEXT);
    }

    default void updateSmallRyeContext(final DataFetchingEnvironment dfe, SmallRyeContext smallRyeContext) {
        GraphQLContext graphQLContext = dfe.getGraphQlContext();
        graphQLContext.put(CONTEXT, smallRyeContext);
    }

    default SmallRyeContext updateSmallRyeContext(final DataFetchingEnvironment dfe, final Field field) {
        SmallRyeContext smallRyeContext = getSmallRyeContext(dfe);
        smallRyeContext = smallRyeContext.withDataFromFetcher(dfe, field);
        updateSmallRyeContext(dfe, smallRyeContext);
        return smallRyeContext;
    }

    default SmallRyeContext updateSmallRyeContext(final BatchLoaderEnvironment ble, final Field field) {
        GraphQLContext graphQLContext = ble.getContext();
        SmallRyeContext smallRyeContext = graphQLContext.get(CONTEXT);
        smallRyeContext = smallRyeContext.withDataFromFetcher(field);
        graphQLContext.put(CONTEXT, smallRyeContext);
        return smallRyeContext;
    }

    default void updateSmallRyeContext(final DataLoader<Object, Object> dataLoader, final DataFetchingEnvironment dfe) {
        // FIXME: this is potentially brittle because it assumes that the batch loader will execute and
        //  consume the context before we call this again for a different operation, but I don't know
        //  how else to pass this context to the matching BatchLoaderEnvironment instance
        SmallRyeBatchLoaderContextProvider.getForDataLoader(dataLoader).set(dfe.getGraphQlContext());
    }
}
