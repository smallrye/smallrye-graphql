package io.smallrye.graphql.execution.datafetcher;

import graphql.schema.DataFetchingEnvironment;
import io.smallrye.graphql.SmallRyeGraphQLServerLogging;
import io.smallrye.graphql.execution.datafetcher.helper.FieldHelper;
import io.smallrye.graphql.schema.model.Field;
import io.smallrye.graphql.transformation.DataFetchingException;

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
    public Object get(DataFetchingEnvironment environment) {
        Object resultFromMethodCall = super.get(environment);
        try {
            // See if we need to transform
            return fieldHelper.transformResponse(resultFromMethodCall);
        } catch (DataFetchingException ex) {
            SmallRyeGraphQLServerLogging.log.transformError(ex);
            return resultFromMethodCall;
        }
    }
}
