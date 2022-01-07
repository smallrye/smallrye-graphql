package io.smallrye.graphql.client.vertx.websocket;

import java.util.concurrent.atomic.AtomicReference;

import javax.json.JsonObject;

import org.jboss.logging.Logger;

import io.smallrye.mutiny.subscription.MultiEmitter;
import io.vertx.core.http.WebSocket;

/**
 * Handler for the dummy smallrye-graphql subscription websocket protocol.
 */
public class BasicSmallRyeGraphQLWebSocketSubprotocolHandler implements WebSocketSubprotocolHandler {

    private static final Logger log = Logger.getLogger(BasicSmallRyeGraphQLWebSocketSubprotocolHandler.class);

    private AtomicReference<WebSocket> webSocketReference = new AtomicReference<>();

    @Override
    public void handleWebSocketStart(
            JsonObject request,
            MultiEmitter<? super String> dataEmitter,
            WebSocket webSocket) {
        this.webSocketReference.set(webSocket);
        webSocket.writeTextMessage(request.toString());
        webSocket.handler(message -> {
            if (!dataEmitter.isCancelled()) {
                dataEmitter.emit(message.toString());
            } else {
                // We still received some more messages after the Emitter got cancelled. This can happen
                // if the server is sending events very quickly and one of them contains an error that can't be applied
                // (and thus fails the client-side Multi with a GraphQLClientException), in which case we close the websocket
                // immediately, but if the server was fast enough, we might have received more messages before actually closing the websocket.
                // But because the Multi has already received a failure, we can't propagate this to the client application anymore.
                // Let's just log it.
                log.warn(
                        "Received an additional item for a subscription that has already ended with a failure, dropping it.");
            }
        });
        webSocket.closeHandler((v) -> dataEmitter.complete());
        dataEmitter.onTermination(webSocket::close);
    }

    @Override
    public void handleCancel() {
        WebSocket webSocket = webSocketReference.get();
        if (webSocket != null) {
            webSocket.close((short) 1000);
            log.debug("Closed the Websocket");
        }
    }
}
