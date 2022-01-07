package io.smallrye.graphql.client.vertx.websocket;

import io.smallrye.graphql.client.vertx.websocket.graphqltransportws.GraphQLTransportWSSubprotocolHandler;
import io.smallrye.graphql.client.vertx.websocket.graphqlws.GraphQLWSSubprotocolHandler;

public class BuiltinWebsocketSubprotocolHandlers {

    public static WebSocketSubprotocolHandler createHandlerFor(String protocolName) {
        switch (protocolName) {
            case "smallrye-graphql":
            case "":
                return new BasicSmallRyeGraphQLWebSocketSubprotocolHandler();
            case "graphql-ws":
                return new GraphQLWSSubprotocolHandler();
            case "graphql-transport-ws":
                return new GraphQLTransportWSSubprotocolHandler();
            default:
                throw new IllegalArgumentException("Unknown subprotocol: " + protocolName);
        }
    }

}
