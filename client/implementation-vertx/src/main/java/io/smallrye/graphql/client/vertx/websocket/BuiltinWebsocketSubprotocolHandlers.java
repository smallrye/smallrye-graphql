package io.smallrye.graphql.client.vertx.websocket;

import io.smallrye.graphql.client.vertx.websocket.graphqltransportws.GraphQLTransportWSSubprotocolHandler;
import io.smallrye.graphql.client.vertx.websocket.graphqlws.GraphQLWSSubprotocolHandler;

public class BuiltinWebsocketSubprotocolHandlers {

    public static WebSocketSubprotocolHandler createHandlerFor(String protocolName, Integer subscriptionInitializationTimeout) {
        switch (protocolName) {
            case "graphql-ws":
                return new GraphQLWSSubprotocolHandler(subscriptionInitializationTimeout);
            case "graphql-transport-ws":
                return new GraphQLTransportWSSubprotocolHandler(subscriptionInitializationTimeout);
            default:
                throw new IllegalArgumentException("Unknown subprotocol: " + protocolName);
        }
    }

}
