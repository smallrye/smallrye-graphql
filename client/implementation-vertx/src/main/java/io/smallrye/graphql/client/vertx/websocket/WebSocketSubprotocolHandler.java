package io.smallrye.graphql.client.vertx.websocket;

import jakarta.json.JsonObject;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.subscription.MultiEmitter;
import io.smallrye.mutiny.subscription.UniEmitter;

/**
 * An implementation of this interface is responsible for handling a particular subscription websocket protocol.
 * It is responsible for registering a handler to listen for messages on the websocket, and then use the provided
 * `MultiEmitter` and `UniEmitter` objects to emit individual responses (or failures).
 */
// TODO: formalize if/when the handler should close the websocket on errors. In some cases, a disconnect
// could be mitigated by reestablishing the websocket connection
public interface WebSocketSubprotocolHandler {

    /**
     * This is called to initialize the websocket connection and prepare it for executing operations.
     * The returned Uni is completed when the websocket is fully initialized (including necessary server ACKs specific to the
     * protocol).
     * If the handler is already fully initialized, this returns a completed Uni.
     */
    Uni<Void> ensureInitialized();

    /**
     * Requests an execution of a single-result operation over the websocket.
     *
     * @param request Request in full JSON format describing the operation to be executed.
     * @param emitter Emitter that should receive the completion event (or an error) when the operation finishes.
     * @return The generated internal ID of this operation.
     */
    String executeUni(JsonObject request, UniEmitter<? super String> emitter);

    /**
     * Requests an execution of a subscription operation over the websocket.
     *
     * @param request Request in full JSON format describing the operation to be executed.
     * @param emitter Emitter that should receive the completion events (or an error) from the subscription.
     * @return The generated internal ID of this operation.
     */
    String executeMulti(JsonObject request, MultiEmitter<? super String> emitter);

    /**
     * Cancels an active single-result operation with the given ID. This does not do anything with the Emitter for this
     * operation,
     * it only sends a cancellation message to the server (if applicable depending on the protocol), and marks this
     * operation as finished.
     *
     * @param operationId ID of the operation (returned from calling `executeUni`)
     */
    void cancelUni(String operationId);

    /**
     * Cancels an active subscription with the given ID. This does not do anything with the Emitter for this operation,
     * it only sends a cancellation message to the server (if applicable depending on the protocol), and marks this
     * operation as finished.
     *
     * @param operationId ID of the operation (returned from calling `executeMulti`)
     */
    void cancelMulti(String operationId);

    /**
     * Called when the websocket should be closed (for example, when the GraphQL client is being closed).
     */
    void close();

}
