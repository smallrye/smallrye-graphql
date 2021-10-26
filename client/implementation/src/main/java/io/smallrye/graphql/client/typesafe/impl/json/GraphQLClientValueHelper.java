package io.smallrye.graphql.client.typesafe.impl.json;

import io.smallrye.graphql.client.InvalidResponseException;

class GraphQLClientValueHelper {

    static void check(Location location, Object value, boolean expression) {
        if (!expression)
            throw new InvalidResponseException(message(location, value));
    }

    static InvalidResponseException fail(Location location, Object value) {
        return new InvalidResponseException(message(location, value));
    }

    private static String message(Location location, Object value) {
        return "invalid " + location + ": " + value;
    }
}
