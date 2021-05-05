package io.smallrye.graphql.client.typesafe.api;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

import java.util.List;

public class GraphQLClientException extends RuntimeException {
    private final List<GraphQLClientError> errors;

    public GraphQLClientException(String message) {
        super(message);
        this.errors = emptyList();
    }

    public GraphQLClientException(String message, List<GraphQLClientError> errors) {
        super(message);
        this.errors = requireNonNull(errors);
    }

    public GraphQLClientException(String message, Throwable cause) {
        super(message, cause);
        this.errors = emptyList();
    }

    @Override
    public String toString() {
        return "GraphQlClientException: " + getMessage() + (errors.isEmpty() ? ""
                : "\nerrors:\n- " + errors.stream().map(GraphQLClientError::toString).collect(joining("\n- "))) + ")";
    }

    public List<GraphQLClientError> getErrors() {
        return errors;
    }
}
