package io.smallrye.graphql.client;

/**
 * Marks a response that had unexpected contents and the client was unable to properly process it.
 * This can be either due to an error on the server side (returning non-conformant responses),
 * but possibly also due to client-side issues, for example
 * a mismatch between model classes and the schema used on the server.
 */
public class InvalidResponseException extends RuntimeException {

    public InvalidResponseException(String message) {
        super(message);
    }

    public InvalidResponseException(String message, Throwable cause) {
        super(message, cause);
    }

}
