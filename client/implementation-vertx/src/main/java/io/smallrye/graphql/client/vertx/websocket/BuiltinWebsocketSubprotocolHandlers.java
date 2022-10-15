package io.smallrye.graphql.client.vertx.websocket;

import java.util.Map;

import io.smallrye.graphql.client.vertx.websocket.graphqltransportws.GraphQLTransportWSSubprotocolHandler;
import io.smallrye.graphql.client.vertx.websocket.graphqlws.GraphQLWSSubprotocolHandler;
import io.vertx.core.http.WebSocket;

public class BuiltinWebsocketSubprotocolHandlers {

    public static WebSocketSubprotocolHandler createHandlerFor(String protocolName, WebSocket webSocket,
            Integer subscriptionInitializationTimeout, Map<String, Object> initPayload, Runnable onClose) {
        switch (protocolName) {
            case "graphql-ws":
                return new GraphQLWSSubprotocolHandler(webSocket, subscriptionInitializationTimeout, initPayload, onClose);
            case "graphql-transport-ws":
                return new GraphQLTransportWSSubprotocolHandler(webSocket, subscriptionInitializationTimeout, initPayload,
                        onClose);
            default:
                throw new IllegalArgumentException("Unknown subprotocol: " + protocolName);
        }
    }

}
