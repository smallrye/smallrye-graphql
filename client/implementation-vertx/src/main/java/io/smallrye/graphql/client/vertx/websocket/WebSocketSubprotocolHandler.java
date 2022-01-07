package io.smallrye.graphql.client.vertx.websocket;

import javax.json.JsonObject;

import io.smallrye.mutiny.subscription.MultiEmitter;
import io.vertx.core.http.WebSocket;

/**
 * An implementation of this interface is responsible for handling a particular subscription websocket protocol.
 * It is responsible for registering a handler to listen for messages on the websocket, and then use the provided
 * `MultiEmitter` to emit individual responses with raw GraphQL data.
 *
 * The implementation is responsible for closing the websocket if it encounters an error or the subscription ends,
 * but it must also always be ready for the case that the websocket gets closed from somewhere else.
 * If the websocket is closed, the data emitter must be closed, and vice versa.
 *
 */
public interface WebSocketSubprotocolHandler {

    /**
     * Called after the websocket channel is successfully initialized. This should register a handler that handles
     * the communication over the websocket and also relay received data through the `dataEmitter`.
     */
    void handleWebSocketStart(JsonObject request, MultiEmitter<? super String> dataEmitter, WebSocket webSocket);

    /**
     * Called when the subscription is cancelled on the client side (by the user).
     */
    void handleCancel();

}
