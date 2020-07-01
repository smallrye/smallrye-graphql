package io.smallrye.graphql.execution.datafetcher;

import static io.smallrye.graphql.SmallRyeGraphQLServerLogging.log;

import graphql.GraphQLContext;
import graphql.schema.DataFetchingEnvironment;
import io.smallrye.graphql.execution.context.SmallRyeContext;
import io.smallrye.graphql.execution.datafetcher.helper.FieldHelper;
import io.smallrye.graphql.schema.model.Field;
import io.smallrye.graphql.transformation.AbstractDataFetcherException;

/**
 * Extending the default property data fetcher to intercept the result for some manipulation
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class PropertyDataFetcher extends graphql.schema.PropertyDataFetcher {

    private final FieldHelper fieldHelper;

    public PropertyDataFetcher(Field field) {
        super(field.getPropertyName());
        this.fieldHelper = new FieldHelper(field);
    }

    @Override
    public Object get(DataFetchingEnvironment dfe) {

        GraphQLContext graphQLContext = dfe.getContext();
        SmallRyeContext src = graphQLContext.get("context");
        src.setDataFetchingEnvironment(dfe);

        Object resultFromMethodCall = super.get(dfe);
        try {
            // See if we need to transform
            return fieldHelper.transformResponse(resultFromMethodCall);
        } catch (AbstractDataFetcherException ex) {
            log.transformError(ex);
            return resultFromMethodCall;
        }
    }
}
