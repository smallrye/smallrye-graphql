package io.smallrye.graphql.client;

/**
 * Marks a close WebSocket message from the server that was unexpected.
 */
public class UnexpectedCloseException extends InvalidResponseException {

    private final int closeStatusCode;

    public UnexpectedCloseException(String message, int closeStatusCode) {
        super(message);
        this.closeStatusCode = closeStatusCode;
    }

    public UnexpectedCloseException(String message, Throwable cause, int closeStatusCode) {
        super(message, cause);
        this.closeStatusCode = closeStatusCode;
    }

    /**
     * The close status code returned by the server.
     */
    public int getCloseStatusCode() {
        return closeStatusCode;
    }
}
