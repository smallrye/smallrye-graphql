package io.smallrye.graphql.client.vertx.websocket.graphqlws;

public enum MessageType {

    // client-> server messages
    GQL_CONNECTION_INIT("connection_init"),
    GQL_START("start"),
    GQL_STOP("stop"),
    GQL_CONNECTION_TERMINATE("connection_terminate"),

    // server->client messages
    GQL_CONNECTION_ERROR("connection_error"),
    GQL_CONNECTION_ACK("connection_ack"),
    GQL_DATA("data"),
    GQL_ERROR("error"),
    GQL_COMPLETE("complete"),
    GQL_CONNECTION_KEEP_ALIVE("ka");

    private String str;

    MessageType(String str) {
        this.str = str;
    }

    public static MessageType fromString(String text) {
        for (MessageType b : MessageType.values()) {
            if (b.str.equalsIgnoreCase(text)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unknown message type: " + text);
    }

    public String asString() {
        return str;
    }
}
