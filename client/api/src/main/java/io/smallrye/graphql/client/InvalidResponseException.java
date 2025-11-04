package io.smallrye.graphql.client;

import java.util.List;
import java.util.Map;

/**
 * Marks a response that had unexpected contents and the client was unable to properly process it.
 * This can be either due to an error on the server side (returning non-conformant responses),
 * but possibly also due to client-side issues, for example
 * a mismatch between model classes and the schema used on the server.
 */
public class InvalidResponseException extends RuntimeException {

    private final Map<String, List<String>> transportMeta;

    public InvalidResponseException(String message) {
        super(message);
        this.transportMeta = null;
    }

    public InvalidResponseException(String message, Throwable cause) {
        super(message, cause);
        this.transportMeta = null;
    }

    public InvalidResponseException(String message, Throwable cause, Map<String, List<String>> transportMeta) {
        super(message, cause);
        this.transportMeta = transportMeta;
    }

    /**
     * Get transport-specific metadata that came from the server with the invalid response.
     * For HTTP, these are the response headers. It can be null if headers aren't applicable, for example
     * if this is coming from a message received over a WebSocket connection.
     */
    public Map<String, List<String>> getTransportMeta() {
        return transportMeta;
    }

}
