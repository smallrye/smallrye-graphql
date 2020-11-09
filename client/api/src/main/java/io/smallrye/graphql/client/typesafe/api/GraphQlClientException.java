package io.smallrye.graphql.client.typesafe.api;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;

import java.util.List;

public class GraphQlClientException extends RuntimeException {
    private final List<GraphQlClientError> errors;

    public GraphQlClientException(String message) {
        super(message);
        this.errors = emptyList();
    }

    public GraphQlClientException(String message, List<GraphQlClientError> errors) {
        super(message);
        this.errors = requireNonNull(errors);
    }

    public GraphQlClientException(String message, Throwable cause) {
        super(message, cause);
        this.errors = emptyList();
    }

    public List<GraphQlClientError> getErrors() {
        return errors;
    }
}
