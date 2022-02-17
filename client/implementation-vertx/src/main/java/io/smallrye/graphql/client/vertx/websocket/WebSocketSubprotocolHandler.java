package io.smallrye.graphql.client.vertx.websocket;

import javax.json.JsonObject;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.subscription.MultiEmitter;
import io.smallrye.mutiny.subscription.UniEmitter;

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
     * This is called to initialize the websocket connection and prepare it for executing operations.
     * The returned Uni is completed when the websocket is fully initialized (including necessary server ACKs specific to the
     * protocol).
     * If the handler is already fully initialized, this returns a completed Uni.
     */
    Uni<Void> ensureInitialized();

    void executeUni(JsonObject request, UniEmitter<? super String> emitter);

    void executeMulti(JsonObject request, MultiEmitter<? super String> emitter);

    /**
     * Called when the websocket should be closed (for example, when the GraphQL client is being closed).
     */
    void close();

}
