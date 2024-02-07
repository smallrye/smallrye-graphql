package io.smallrye.graphql.client.websocket;

public enum WebsocketSubprotocol {

    GRAPHQL_WS("graphql-ws"),
    GRAPHQL_TRANSPORT_WS("graphql-transport-ws");

    private static final WebsocketSubprotocol[] VALUES = values();

    private final String protocolId;

    WebsocketSubprotocol(String protocolId) {
        this.protocolId = protocolId;
    }

    public String getProtocolId() {
        return protocolId;
    }

    public static WebsocketSubprotocol fromString(String text) {
        for (WebsocketSubprotocol b : VALUES) {
            if (b.protocolId.equalsIgnoreCase(text)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unknown websocket subprotocol: " + text);
    }
}
