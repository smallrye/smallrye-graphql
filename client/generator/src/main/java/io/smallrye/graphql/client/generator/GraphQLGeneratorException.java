package io.smallrye.graphql.client.generator;

public class GraphQLGeneratorException extends RuntimeException {
    public GraphQLGeneratorException(String message) {
        super(message);
    }

    public GraphQLGeneratorException(String message, Throwable cause) {
        super(message, cause);
    }
}
