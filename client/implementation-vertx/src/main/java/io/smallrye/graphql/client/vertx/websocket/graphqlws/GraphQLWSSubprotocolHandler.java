package io.smallrye.graphql.client.vertx.websocket.graphqlws;

import static io.smallrye.graphql.client.impl.JsonProviderHolder.JSON_PROVIDER;

import java.io.StringReader;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.json.JsonBuilderFactory;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonReader;
import jakarta.json.JsonReaderFactory;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import jakarta.json.stream.JsonParsingException;

import org.jboss.logging.Logger;

import io.smallrye.graphql.client.GraphQLClientException;
import io.smallrye.graphql.client.GraphQLError;
import io.smallrye.graphql.client.InvalidResponseException;
import io.smallrye.graphql.client.UnexpectedCloseException;
import io.smallrye.graphql.client.impl.ResponseReader;
import io.smallrye.graphql.client.vertx.websocket.WebSocketSubprotocolHandler;
import io.smallrye.graphql.client.vertx.websocket.opid.IncrementingNumberOperationIDGenerator;
import io.smallrye.graphql.client.vertx.websocket.opid.OperationIDGenerator;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.subscription.Cancellable;
import io.smallrye.mutiny.subscription.MultiEmitter;
import io.smallrye.mutiny.subscription.UniEmitter;
import io.vertx.core.http.WebSocket;

/**
 * Handler for the legacy `graphql-ws` subprotocol
 * Specification of the protocol: `https://github.com/apollographql/subscriptions-transport-ws/blob/master/PROTOCOL.md`
 */
public class GraphQLWSSubprotocolHandler implements WebSocketSubprotocolHandler {

    private static final Logger log = Logger.getLogger(GraphQLWSSubprotocolHandler.class);
    private static final JsonBuilderFactory jsonBuilderFactory = JSON_PROVIDER.createBuilderFactory(null);
    private static final JsonReaderFactory jsonReaderFactory = JSON_PROVIDER.createReaderFactory(null);

    private final Integer subscriptionInitializationTimeout;

    private final WebSocket webSocket;
    private final CompletableFuture<Void> initialization;

    private final Map<String, UniEmitter<? super String>> uniOperations;
    private final Map<String, MultiEmitter<? super String>> multiOperations;

    private final Runnable onClose;

    private final OperationIDGenerator operationIdGenerator;
    private final Map<String, Object> initPayload;

    public GraphQLWSSubprotocolHandler(WebSocket webSocket, Integer subscriptionInitializationTimeout,
            Map<String, Object> initPayload, Runnable onClose) {
        this.initPayload = new HashMap<>();
        if (initPayload != null) {
            this.initPayload.putAll(initPayload);
        }
        this.webSocket = webSocket;
        this.subscriptionInitializationTimeout = subscriptionInitializationTimeout;
        this.uniOperations = new ConcurrentHashMap<>();
        this.multiOperations = new ConcurrentHashMap<>();
        this.initialization = initialize().subscribeAsCompletionStage();
        this.onClose = onClose;
        this.operationIdGenerator = new IncrementingNumberOperationIDGenerator();
    }

    private Uni<Void> initialize() {
        return Uni.createFrom().emitter(initializationEmitter -> {
            if (log.isTraceEnabled()) {
                log.trace("Initializing websocket with graphql-ws protocol");
            }

            webSocket.closeHandler((v) -> {
                onClose.run();
                Exception exception;
                if (webSocket.closeStatusCode() != null) {
                    if (webSocket.closeStatusCode() == 1000) {
                        log.debug("WebSocket closed with status code 1000");
                        exception = new UnexpectedCloseException("Connection closed before data was received", 1000);
                        // even if the status code is OK, any unfinished single-result operation
                        // should be marked as failed
                        uniOperations.forEach((id, emitter) -> emitter.fail(exception));
                        multiOperations.forEach((id, emitter) -> emitter.complete());
                    } else {
                        exception = new UnexpectedCloseException(
                                "Server closed the websocket connection with code: "
                                        + webSocket.closeStatusCode() + " and reason: " + webSocket.closeReason(),
                                webSocket.closeStatusCode());
                        uniOperations.forEach((id, emitter) -> emitter.fail(exception));
                        multiOperations.forEach((id, emitter) -> emitter.fail(exception));
                    }
                } else {
                    exception = new InvalidResponseException("Connection closed");
                    uniOperations.forEach((id, emitter) -> emitter.fail(exception));
                    multiOperations.forEach((id, emitter) -> emitter.fail(exception));
                }

                initializationEmitter.fail(exception);
            });
            webSocket.exceptionHandler(this::failAllActiveOperationsWith);

            send(webSocket, createConnectionInitMessage());

            // set up a timeout for subscription initialization
            Cancellable timeoutWaitingForConnectionAckMessage = null;
            if (subscriptionInitializationTimeout != null) {
                timeoutWaitingForConnectionAckMessage = Uni.createFrom().item(1).onItem().delayIt()
                        .by(Duration.ofMillis(subscriptionInitializationTimeout))
                        .subscribe().with(timeout -> {
                            initializationEmitter
                                    .fail(new InvalidResponseException("Server did not send a connection_ack message"));
                            webSocket.close((short) 1002, "Timeout waiting for a connection_ack message");
                        });
            }
            // make an effectively final copy of this value to use it in a lambda expression
            Cancellable finalTimeoutWaitingForConnectionAckMessage = timeoutWaitingForConnectionAckMessage;

            webSocket.textMessageHandler(text -> {
                if (log.isTraceEnabled()) {
                    log.trace("<<< " + text);
                }
                try {
                    JsonObject message = parseIncomingMessage(text);
                    MessageType messageType = getMessageType(message);
                    switch (messageType) {
                        case GQL_CONNECTION_ERROR:
                            failAllActiveOperationsWith(new InvalidResponseException(message.get("payload").toString()));
                            webSocket.close();
                            break;
                        case GQL_CONNECTION_ACK:
                            if (finalTimeoutWaitingForConnectionAckMessage != null) {
                                finalTimeoutWaitingForConnectionAckMessage.cancel();
                            }
                            initializationEmitter.complete(null);
                            break;
                        case GQL_DATA:
                            handleData(message.getString("id"), message.getJsonObject("payload"));
                            break;
                        case GQL_ERROR:
                            handleOperationError(message.getString("id"), message.getJsonObject("payload"));
                            break;
                        case GQL_COMPLETE:
                            handleComplete(message.getString("id"));
                            break;
                        case GQL_START:
                        case GQL_STOP:
                        case GQL_CONNECTION_KEEP_ALIVE:
                        case GQL_CONNECTION_INIT:
                        case GQL_CONNECTION_TERMINATE:
                            break;
                    }
                } catch (JsonParsingException | IllegalArgumentException e) {
                    log.error("Unexpected message from server: " + text);
                    // should we fail the operations here?
                }
            });
        });
    }

    @Override
    public Uni<Void> ensureInitialized() {
        return Uni.createFrom().completionStage(initialization);
    }

    @Override
    public String executeUni(JsonObject request, UniEmitter<? super String> emitter) {
        String id = operationIdGenerator.generate();
        ensureInitialized().subscribe().with(ready -> {
            uniOperations.put(id, emitter);
            JsonObject subscribe = createSubscribeMessage(request, id);
            send(webSocket, subscribe);
        }, emitter::fail);
        return id;
    }

    @Override
    public String executeMulti(JsonObject request, MultiEmitter<? super String> emitter) {
        String id = operationIdGenerator.generate();
        ensureInitialized().subscribe().with(ready -> {
            multiOperations.put(id, emitter);
            JsonObject subscribe = createSubscribeMessage(request, id);
            send(webSocket, subscribe);
        }, emitter::fail);
        return id;
    }

    @Override
    public void cancelUni(String id) {
        uniOperations.remove(id);
        send(webSocket, createStopMessage(id));
    }

    @Override
    public void cancelMulti(String id) {
        multiOperations.remove(id);
        send(webSocket, createStopMessage(id));
    }

    @Override
    public void close() {
        if (webSocket != null && !webSocket.isClosed()) {
            send(webSocket, createConnectionTerminateMessage()).subscribe().with(complete -> {
                webSocket.close((short) 1000);
            });
        }
    }

    private JsonObject parseIncomingMessage(String message) {
        try (JsonReader jsonReader = jsonReaderFactory.createReader(new StringReader(message))) {
            return jsonReader.readObject();
        }
    }

    private MessageType getMessageType(JsonObject message) {
        return MessageType.fromString(message.getString("type"));
    }

    private Uni<Void> send(WebSocket webSocket, JsonObject message) {
        String string = message.toString();
        if (log.isTraceEnabled()) {
            log.trace(">>> " + string);
        }
        return Uni.createFrom().completionStage(webSocket.writeTextMessage(string).toCompletionStage());
    }

    private JsonObject createConnectionInitMessage() {
        JsonObjectBuilder payloadBuilder = jsonBuilderFactory.createObjectBuilder();
        if (!initPayload.isEmpty()) {
            payloadBuilder.add("payload", jsonBuilderFactory.createObjectBuilder(initPayload));
        }
        return jsonBuilderFactory.createObjectBuilder()
                .add("type", "connection_init")
                .addAll(payloadBuilder)
                .build();
    }

    private JsonObject createStopMessage(String id) {
        return jsonBuilderFactory.createObjectBuilder()
                .add("type", "stop")
                .add("id", id)
                .build();
    }

    private JsonObject createConnectionTerminateMessage() {
        return jsonBuilderFactory.createObjectBuilder()
                .add("type", "connection_terminate")
                .build();
    }

    private JsonObject createSubscribeMessage(JsonObject request, String id) {
        JsonObjectBuilder payload = jsonBuilderFactory.createObjectBuilder();

        payload.add("query", request.getString("query"));
        JsonValue operationName = request.get("operationName");
        if (operationName instanceof JsonString) {
            payload.add("operationName", operationName);
        }
        JsonObject variables = request.getJsonObject("variables");
        if (variables != null) {
            payload.add("variables", variables);
        }
        return jsonBuilderFactory.createObjectBuilder()
                .add("type", "start")
                .add("id", id)
                .add("payload", payload)
                .build();
    }

    private void failAllActiveOperationsWith(Throwable throwable) {
        log.debug("Failing all active operations");
        for (String s : uniOperations.keySet()) {
            UniEmitter<? super String> emitter = uniOperations.remove(s);
            if (emitter != null) {
                emitter.fail(throwable);
            }
        }
        for (String s : multiOperations.keySet()) {
            MultiEmitter<? super String> emitter = multiOperations.remove(s);
            if (emitter != null) {
                emitter.fail(throwable);
            }
        }
    }

    private void handleData(String operationId, JsonObject data) {
        // If this is a uni operation, we remove it right away from the active operation map,
        // even though we still should receive a 'complete' message later - we don't wait for it.
        // This is to prevent a potential memory leak in case that the server doesn't actually send it.
        UniEmitter<? super String> uniEmitter = uniOperations.remove(operationId);
        if (uniEmitter != null) {
            if (log.isTraceEnabled()) {
                log.trace("Received data for single-result operation " + operationId);
            }
            uniEmitter.complete(data.toString());
        } else {
            MultiEmitter<? super String> multiEmitter = multiOperations.get(operationId);
            if (multiEmitter != null) {
                if (multiEmitter.isCancelled()) {
                    log.warn("Received data for already cancelled operation " + operationId);
                } else {
                    multiEmitter.emit(data.toString());
                }
            } else {
                log.warn("Received event for an unknown subscription ID: " + operationId);
            }
        }
    }

    private void handleOperationError(String operationId, JsonObject error) {
        GraphQLError parsedError = ResponseReader.readError(error);
        GraphQLClientException exception = new GraphQLClientException("Received an error", parsedError);
        UniEmitter<? super String> emitter = uniOperations.remove(operationId);
        if (emitter != null) {
            emitter.fail(exception);
        } else {
            MultiEmitter<? super String> multiEmitter = multiOperations.remove(operationId);
            if (multiEmitter != null) {
                multiEmitter.fail(exception);
            }
        }
    }

    private void handleComplete(String operationId) {
        UniEmitter<? super String> emitter = uniOperations.remove(operationId);
        if (emitter != null) {
            // For a uni operation, we should have received a 'next' message before the 'complete' message.
            // If that happened, the emitter was already completed and operation removed from the map.
            // If that didn't happen, then this is an issue with the server, let's fail the operation then.
            emitter.fail(new InvalidResponseException("Protocol error: received a 'complete' message for" +
                    " this operation before the actual data"));
        } else {
            MultiEmitter<? super String> multiEmitter = multiOperations.remove(operationId);
            if (multiEmitter != null) {
                log.debug("Completed operation " + operationId);
                multiEmitter.complete();
            }
        }
    }

}
