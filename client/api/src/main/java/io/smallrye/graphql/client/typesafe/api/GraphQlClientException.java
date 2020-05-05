package io.smallrye.graphql.client.typesafe.api;

public class GraphQlClientException extends RuntimeException {
    public GraphQlClientException(String message) {
        super(message);
    }

    public GraphQlClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
