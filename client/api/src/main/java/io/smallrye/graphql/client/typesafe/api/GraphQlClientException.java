package io.smallrye.graphql.client.typesafe.api;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

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

    @Override
    public String toString() {
        return "GraphQlClientException: " + getMessage() + (errors.isEmpty() ? ""
                : "\nerrors:\n- " + errors.stream().map(GraphQlClientError::toString).collect(joining("\n- "))) + ")";
    }

    public List<GraphQlClientError> getErrors() {
        return errors;
    }
}
