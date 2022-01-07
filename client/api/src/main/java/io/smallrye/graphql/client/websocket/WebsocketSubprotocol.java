package io.smallrye.graphql.client.websocket;

public enum WebsocketSubprotocol {

    SMALLRYE_GRAPHQL("smallrye-graphql"),
    GRAPHQL_WS("graphql-ws"),
    GRAPHQL_TRANSPORT_WS("graphql-transport-ws");

    private String protocolId;

    WebsocketSubprotocol(String protocolId) {
        this.protocolId = protocolId;
    }

    public String getProtocolId() {
        return protocolId;
    }

    public static WebsocketSubprotocol fromString(String text) {
        for (WebsocketSubprotocol b : WebsocketSubprotocol.values()) {
            if (b.protocolId.equalsIgnoreCase(text)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unknown websocket subprotocol: " + text);
    }
}
