package io.smallrye.graphql.execution.datafetcher.helper;

import org.dataloader.BatchLoaderEnvironment;

import graphql.GraphQLContext;
import graphql.schema.DataFetchingEnvironment;
import io.smallrye.graphql.execution.context.SmallRyeContext;
import io.smallrye.graphql.schema.model.Field;

/**
 * Helping with context
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class ContextHelper {

    public static final String CONTEXT = "context";

    public SmallRyeContext getSmallRyeContext(final BatchLoaderEnvironment ble) {
        DataFetchingEnvironment dfe = ble.getContext();
        return getSmallRyeContext(dfe);
    }

    public SmallRyeContext getSmallRyeContext(final DataFetchingEnvironment dfe) {
        GraphQLContext graphQLContext = dfe.getGraphQlContext();
        return graphQLContext.get(CONTEXT);
    }

    public void setSmallRyeContext(final DataFetchingEnvironment dfe, SmallRyeContext smallRyeContext) {
        GraphQLContext graphQLContext = dfe.getGraphQlContext();
        graphQLContext.put(CONTEXT, smallRyeContext);
    }

    public SmallRyeContext updateSmallRyeContextWithField(final DataFetchingEnvironment dfe, final Field field) {
        SmallRyeContext smallRyeContext = getSmallRyeContext(dfe);
        smallRyeContext = smallRyeContext.withDataFromFetcher(dfe, field);
        setSmallRyeContext(dfe, smallRyeContext);
        return smallRyeContext;
    }

    public SmallRyeContext updateSmallRyeContextWithField(final BatchLoaderEnvironment ble, final Field field) {
        DataFetchingEnvironment dfe = ble.getContext();
        return updateSmallRyeContextWithField(dfe, field);
    }
}
