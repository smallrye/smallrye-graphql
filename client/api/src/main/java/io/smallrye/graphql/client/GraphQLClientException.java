package io.smallrye.graphql.client;

import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents a response that contained application-level errors and thus can't be turned into a domain object.
 * Thrown by the typesafe client upon an invocation.
 * This does NOT represent a response that can't be properly parsed, only a response that contains
 * application-level errors.
 */
public class GraphQLClientException extends RuntimeException {
    private final List<GraphQLError> errors;

    public GraphQLClientException(String message, GraphQLError error) {
        super(message);
        this.errors = singletonList(requireNonNull(error));
    }

    public GraphQLClientException(String message, List<GraphQLError> errors) {
        super(message);
        this.errors = requireNonNull(errors).stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "GraphQlClientException: " + getMessage() + (errors.isEmpty() ? ""
                : "\nerrors:\n- " + errors.stream().map(GraphQLError::toString).collect(joining("\n- "))) + ")";
    }

    public List<GraphQLError> getErrors() {
        return errors;
    }
}
