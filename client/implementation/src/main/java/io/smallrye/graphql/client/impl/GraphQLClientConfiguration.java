package io.smallrye.graphql.client.impl;

import java.util.List;
import java.util.Map;

/**
 * The configuration of a single GraphQL client.
 */
public class GraphQLClientConfiguration {

    /**
     * The URL that the client connects to.
     */
    private String url;

    /**
     * HTTP headers to be appended to each HTTP request.
     */
    private Map<String, String> headers;

    /**
     * Names of websocket subprotocols that this client will understand. The actual subprotocol to be used
     * will be subject to negotiation with the server.
     */
    private List<String> websocketSubprotocols;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public List<String> getWebsocketSubprotocols() {
        return websocketSubprotocols;
    }

    public void setWebsocketSubprotocols(List<String> websocketSubprotocols) {
        this.websocketSubprotocols = websocketSubprotocols;
    }

    GraphQLClientConfiguration merge(GraphQLClientConfiguration other) {
        if (this.url == null) {
            this.url = other.url;
        }
        if (this.headers == null) {
            this.headers = other.headers;
        } else if (other.headers != null) {
            other.headers.forEach((key, value) -> this.headers.putIfAbsent(key, value));
        }
        if (this.websocketSubprotocols == null) {
            this.websocketSubprotocols = other.websocketSubprotocols;
        } else if (other.websocketSubprotocols != null) {
            this.websocketSubprotocols.addAll(other.websocketSubprotocols);
        }
        return this;
    }
}
