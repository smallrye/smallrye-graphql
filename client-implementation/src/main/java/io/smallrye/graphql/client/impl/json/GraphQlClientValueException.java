package io.smallrye.graphql.client.impl.json;

import io.smallrye.graphql.client.api.GraphQlClientException;

class GraphQlClientValueException extends GraphQlClientException {
    static void check(Location location, Object value, boolean expression) {
        if (!expression)
            throw new GraphQlClientValueException(location, value);
    }

    GraphQlClientValueException(Location location, Object value) {
        super(message(location, value));
    }

    GraphQlClientValueException(Location location, Object value, Throwable cause) {
        super(message(location, value), cause);
    }

    private static String message(Location location, Object value) {
        return "invalid " + location + ": " + value;
    }
}
