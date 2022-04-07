package io.smallrye.graphql.execution.datafetcher.helper;

import org.dataloader.BatchLoaderEnvironment;

import graphql.GraphQLContext;
import graphql.schema.DataFetchingEnvironment;
import io.smallrye.graphql.execution.context.SmallRyeContext;
import io.smallrye.graphql.schema.model.Field;
import io.smallrye.graphql.schema.model.Type;

/**
 * Helping with context
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class ContextHelper {

    public static final String CONTEXT = "context";

    public SmallRyeContext getSmallRyeContext(final BatchLoaderEnvironment ble) {
        SmallRyeContext smallRyeContext = ble.getContext();
        return smallRyeContext;
    }

    public SmallRyeContext getSmallRyeContext(final DataFetchingEnvironment dfe) {
        GraphQLContext graphQLContext = dfe.getGraphQlContext();
        return graphQLContext.get(CONTEXT);
    }

    public GraphQLContext getGraphQLContext(final DataFetchingEnvironment dfe) {
        return dfe.getGraphQlContext();
    }

    public void setSmallRyeContext(final DataFetchingEnvironment dfe, SmallRyeContext smallRyeContext) {
        GraphQLContext graphQLContext = dfe.getGraphQlContext();
        graphQLContext.put(CONTEXT, smallRyeContext);
    }

    public SmallRyeContext updateSmallRyeContextWithField(final DataFetchingEnvironment dfe, final Field field,
            final Type type) {
        SmallRyeContext smallRyeContext = getSmallRyeContext(dfe);
        smallRyeContext = smallRyeContext.withDataFromFetcher(dfe, field, type);
        setSmallRyeContext(dfe, smallRyeContext);
        return smallRyeContext;
    }

}
