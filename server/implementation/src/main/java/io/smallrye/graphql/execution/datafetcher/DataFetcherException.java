package io.smallrye.graphql.execution.datafetcher;

import io.smallrye.graphql.schema.model.Operation;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

/**
 * There was an issue when fetching data.
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class DataFetcherException extends RuntimeException {
    private static final ObjectMapper OBJECT_MAPPER = JsonMapper.builder()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .build();

    public DataFetcherException() {
    }

    public DataFetcherException(Operation operation) {
        super("Problem while fetching data for operation \n" + toJson(operation));
    }

    public DataFetcherException(Operation operation, Exception ex) {
        super("Problem while fetching data for operation \n" + toJson(operation), ex);
    }

    private static String toJson(Operation operation) {
        try {
            return OBJECT_MAPPER.writeValueAsString(operation);
        } catch (JacksonException e) {
            return operation.toString();
        }
    }
}
