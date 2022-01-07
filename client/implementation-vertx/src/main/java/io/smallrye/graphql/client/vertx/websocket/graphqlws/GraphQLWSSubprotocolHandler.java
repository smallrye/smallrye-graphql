package io.smallrye.graphql.client.vertx.websocket.graphqlws;

import static io.smallrye.graphql.client.vertx.websocket.graphqlws.MessageType.GQL_CONNECTION_INIT;

import java.io.StringReader;
import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

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
 * Handler for the legacy `graphql-ws` subprotocol
 * Specification of the protocol: `https://github.com/apollographql/subscriptions-transport-ws/blob/master/PROTOCOL.md`
 */
public class GraphQLWSSubprotocolHandler implements WebSocketSubprotocolHandler {

    private static final Logger log = Logger.getLogger(GraphQLWSSubprotocolHandler.class);

    // right now we only use one subscription per connection, so we always name it "1"
    private static final String SUBSCRIPTION_ID = "1";

    private AtomicReference<WebSocket> webSocketReference = new AtomicReference<>();

    @Override
    public void handleWebSocketStart(JsonObject request, MultiEmitter<? super String> dataEmitter, WebSocket webSocket) {
        log.trace("Initializing subscription over graphql-ws protocol with request: " + request.toString());
        this.webSocketReference.set(webSocket);

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

        send(webSocket, createConnectionInitMessage());
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
                    case GQL_CONNECTION_ERROR:
                        dataEmitter.fail(new InvalidResponseException("Error from server: " + message.get("payload")));
                        break;
                    case GQL_CONNECTION_ACK:
                        timeoutWaitingForConnectionAckMessage.cancel();
                        send(webSocket, createSubscribeMessage(request, SUBSCRIPTION_ID));
                        break;
                    case GQL_DATA:
                        String id = message.getString("id");
                        if (!id.equals(SUBSCRIPTION_ID)) {
                            dataEmitter.fail(
                                    new InvalidResponseException("Received event for an unexpected subscription ID: " + id));
                            break;
                        }
                        JsonObject data = message.getJsonObject("payload");
                        dataEmitter.emit(data.toString());
                        break;
                    case GQL_ERROR:
                        GraphQLError error = ResponseReader.readError(message.getJsonObject("payload"));
                        dataEmitter.fail(new GraphQLClientException("Received an error", Collections.singletonList(error)));
                        break;
                    case GQL_COMPLETE:
                        dataEmitter.complete();
                        break;
                    case GQL_START:
                    case GQL_STOP:
                    case GQL_CONNECTION_KEEP_ALIVE:
                    case GQL_CONNECTION_INIT:
                    case GQL_CONNECTION_TERMINATE:
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
            send(webSocket, createStopMessage(SUBSCRIPTION_ID));
            send(webSocket, createConnectionTerminateMessage());
            webSocket.close((short) 1000);
        }
    }

    private JsonObject parseIncomingMessage(String message) {
        return Json.createReader(new StringReader(message)).readObject();
    }

    private MessageType getMessageType(JsonObject message) {
        return MessageType.fromString(message.getString("type"));
    }

    private void send(WebSocket webSocket, JsonObject message) {
        String string = message.toString();
        if (log.isTraceEnabled()) {
            log.trace(">>> " + string);
        }
        webSocket.writeTextMessage(string);
    }

    private JsonObject createConnectionInitMessage() {
        return Json.createObjectBuilder()
                .add("type", "connection_init")
                .build();
    }

    private JsonObject createStopMessage(String id) {
        return Json.createObjectBuilder()
                .add("type", "stop")
                .add("id", id)
                .build();
    }

    private JsonObject createConnectionTerminateMessage() {
        return Json.createObjectBuilder()
                .add("type", "connection_terminate")
                .build();
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
                .add("type", "start")
                .add("id", id)
                .add("payload", payload)
                .build();
    }

}
