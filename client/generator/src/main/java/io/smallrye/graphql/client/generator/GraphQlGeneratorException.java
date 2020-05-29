package io.smallrye.graphql.client.generator;

public class GraphQlGeneratorException extends RuntimeException {
    public GraphQlGeneratorException(String message) {
        super(message);
    }

    public GraphQlGeneratorException(String message, Throwable cause) {
        super(message, cause);
    }
}
