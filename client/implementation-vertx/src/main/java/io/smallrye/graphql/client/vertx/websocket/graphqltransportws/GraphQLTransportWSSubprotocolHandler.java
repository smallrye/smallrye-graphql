package io.smallrye.graphql.client.vertx.websocket.graphqltransportws;

import java.io.StringReader;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;
import javax.json.JsonValue;

import org.jboss.logging.Logger;

import io.smallrye.graphql.client.GraphQLClientException;
import io.smallrye.graphql.client.GraphQLError;
import io.smallrye.graphql.client.InvalidResponseException;
import io.smallrye.graphql.client.impl.ResponseReader;
import io.smallrye.graphql.client.vertx.websocket.WebSocketSubprotocolHandler;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.subscription.Cancellable;
import io.smallrye.mutiny.subscription.MultiEmitter;
import io.vertx.core.http.WebSocket;

/**
 * Implementation of the `graphql-transport-ws` protocol. The protocol specification is at
 * https://github.com/enisdenjo/graphql-ws/blob/master/PROTOCOL.md
 */
public class GraphQLTransportWSSubprotocolHandler implements WebSocketSubprotocolHandler {

    private static final Logger log = Logger.getLogger(GraphQLTransportWSSubprotocolHandler.class);

    // right now we only use one subscription per connection, so we always name it "1"
    private static final String SUBSCRIPTION_ID = "1";

    private final AtomicReference<WebSocket> webSocketReference = new AtomicReference<>();

    // messages that don't change over time can be initialized once at startup
    private JsonObject connectionInitMessage;
    private JsonObject pongMessage;

    @Override
    public void handleWebSocketStart(JsonObject request, MultiEmitter<? super String> dataEmitter, WebSocket webSocket) {
        log.trace("Initializing subscription over graphql-transport-ws protocol with request: " + request.toString());
        this.webSocketReference.set(webSocket);

        connectionInitMessage = Json.createObjectBuilder().add("type", "connection_init").build();
        pongMessage = Json.createObjectBuilder().add("type", "pong")
                .add("payload", Json.createObjectBuilder().add("message", "keepalive")).build();

        webSocket.closeHandler((v) -> {
            if (webSocket.closeStatusCode() != null) {
                if (webSocket.closeStatusCode() == 1000) {
                    log.debug("Subscription finished successfully, the server closed the connection with status code 1000");
                    dataEmitter.complete();
                } else {
                    dataEmitter.fail(
                            new InvalidResponseException("Server closed the websocket connection with code: "
                                    + webSocket.closeStatusCode() + " and reason: " + webSocket.closeReason()));
                }
            } else {
                dataEmitter.complete();
            }
        });
        webSocket.exceptionHandler(dataEmitter::fail);
        dataEmitter.onTermination(webSocket::close);

        send(webSocket, connectionInitMessage);
        // If the server does not send a connection_ack message within 30 seconds, disconnect
        // TODO: Make the timeout configurable?
        Cancellable timeoutWaitingForConnectionAckMessage = Uni.createFrom().item(1).onItem().delayIt()
                .by(Duration.ofSeconds(30))
                .subscribe().with(timeout -> {
                    dataEmitter.fail(new InvalidResponseException("Sever did not send a connection_ack message"));
                    webSocket.close((short) 1002, "Timeout waiting for a connection_ack message");
                });

        webSocket.handler(text -> {
            if (!dataEmitter.isCancelled()) {
                if (log.isTraceEnabled()) {
                    log.trace("<<< " + text);
                }
                JsonObject message = parseIncomingMessage(text.toString());
                MessageType messageType = getMessageType(message);
                switch (messageType) {
                    case PING:
                        send(webSocket, pongMessage);
                        break;
                    case CONNECTION_ACK:
                        timeoutWaitingForConnectionAckMessage.cancel();
                        send(webSocket, createSubscribeMessage(request, SUBSCRIPTION_ID));
                        break;
                    case NEXT:
                        String id = message.getString("id");
                        if (!id.equals(SUBSCRIPTION_ID)) {
                            dataEmitter.fail(
                                    new InvalidResponseException("Received event for an unexpected subscription ID: " + id));
                        }
                        JsonObject data = message.getJsonObject("payload");
                        dataEmitter.emit(data.toString());
                        break;
                    case ERROR:
                        List<GraphQLError> errors = message.getJsonArray("payload")
                                .stream().map(ResponseReader::readError).collect(Collectors.toList());
                        dataEmitter.fail(new GraphQLClientException("Received an error", errors));
                        break;
                    case COMPLETE:
                        dataEmitter.complete();
                        break;
                    case CONNECTION_INIT:
                    case PONG:
                    case SUBSCRIBE:
                        break;
                }
            } else {
                log.warn(
                        "Received an additional item for a subscription that has already ended with a failure, dropping it.");
            }
        });
    }

    @Override
    public void handleCancel() {
        WebSocket webSocket = this.webSocketReference.get();
        if (webSocket != null && !webSocket.isClosed()) {
            send(webSocket, createCompleteMessage(SUBSCRIPTION_ID));
            webSocket.close((short) 1000);
        }
    }

    private MessageType getMessageType(JsonObject message) {
        return MessageType.fromString(message.getString("type"));
    }

    private JsonObject parseIncomingMessage(String message) {
        return Json.createReader(new StringReader(message)).readObject();
    }

    private JsonObject createSubscribeMessage(JsonObject request, String id) {
        JsonObjectBuilder payload = Json.createObjectBuilder();

        payload.add("query", request.getString("query"));
        JsonValue operationName = request.get("operationName");
        if (operationName instanceof JsonString) {
            payload.add("operationName", operationName);
        }
        JsonObject variables = request.getJsonObject("variables");
        if (variables != null) {
            payload.add("variables", variables);
        }
        return Json.createObjectBuilder()
                .add("type", "subscribe")
                .add("id", id)
                .add("payload", payload)
                .build();
    }

    private JsonObject createCompleteMessage(String id) {
        return Json.createObjectBuilder()
                .add("type", "complete")
                .add("id", id)
                .build();
    }

    private void send(WebSocket webSocket, JsonObject message) {
        String string = message.toString();
        if (log.isTraceEnabled()) {
            log.trace(">>> " + string);
        }
        webSocket.writeTextMessage(string);
    }

}
