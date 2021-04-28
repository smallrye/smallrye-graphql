package io.smallrye.graphql.client.typesafe.impl.json;

import io.smallrye.graphql.client.typesafe.api.GraphQLClientException;

class GraphQLClientValueException extends GraphQLClientException {
    static void check(Location location, Object value, boolean expression) {
        if (!expression)
            throw new GraphQLClientValueException(location, value);
    }

    GraphQLClientValueException(Location location, Object value) {
        super(message(location, value));
    }

    GraphQLClientValueException(Location location, Object value, Throwable cause) {
        super(message(location, value), cause);
    }

    private static String message(Location location, Object value) {
        return "invalid " + location + ": " + value;
    }
}
