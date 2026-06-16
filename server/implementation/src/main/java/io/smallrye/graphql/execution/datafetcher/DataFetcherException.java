package io.smallrye.graphql.execution.datafetcher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import io.smallrye.graphql.schema.model.Operation;

/**
 * There was an issue when fetching data.
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class DataFetcherException extends RuntimeException {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

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
        } catch (JsonProcessingException e) {
            return operation.toString();
        }
    }
}
