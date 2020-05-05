package io.smallrye.graphql.execution.datafetcher;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;

import io.smallrye.graphql.schema.model.Operation;

/**
 * There was an issue when fetching data.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class DataFetcherException extends RuntimeException {
    private static final Jsonb JSONB = JsonbBuilder.create(new JsonbConfig().withFormatting(true));

    public DataFetcherException(Operation operation, Exception ex) {
        super("Problem while fetching data for operation \n" + JSONB.toJson(operation), ex);
    }

}
