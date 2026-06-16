package io.smallrye.graphql.client.vertx.websocket.graphqltransportws;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.jboss.logging.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.smallrye.graphql.client.GraphQLClientException;
import io.smallrye.graphql.client.GraphQLError;
import io.smallrye.graphql.client.InvalidResponseException;
import io.smallrye.graphql.client.UnexpectedCloseException;
import io.smallrye.graphql.client.impl.RequestImpl;
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
 * Implementation of the `graphql-transport-ws` protocol. The protocol specification is at
 * https://github.com/enisdenjo/graphql-ws/blob/master/PROTOCOL.md
 */
public class GraphQLTransportWSSubprotocolHandler implements WebSocketSubprotocolHandler {

    private static final Logger log = Logger.getLogger(GraphQLTransportWSSubprotocolHandler.class);
    private static final ObjectMapper MAPPER = RequestImpl.MAPPER;

    private final Integer connectionInitializationTimeout;

    private ObjectNode connectionInitMessage;
    private ObjectNode pongMessage;

    private final WebSocket webSocket;
    private final CompletableFuture<Void> initialization;

    private final Map<String, UniEmitter<? super String>> uniOperations;
    private final Map<String, MultiEmitter<? super String>> multiOperations;

    private final Runnable onClose;

    private final OperationIDGenerator operationIdGenerator;
    private final Map<String, Object> initPayload;

    public GraphQLTransportWSSubprotocolHandler(WebSocket webSocket, Integer subscriptionInitializationTimeout,
            Map<String, Object> initPayload, Runnable onClose) {
        this.initPayload = new HashMap<>();
        if (initPayload != null) {
            this.initPayload.putAll(initPayload);
        }
        this.webSocket = webSocket;
        this.connectionInitializationTimeout = subscriptionInitializationTimeout;
        this.uniOperations = new ConcurrentHashMap<>();
        this.multiOperations = new ConcurrentHashMap<>();
        this.initialization = initialize().subscribeAsCompletionStage();
        this.onClose = onClose;
        this.operationIdGenerator = new IncrementingNumberOperationIDGenerator();
    }

    @Override
    public Uni<Void> ensureInitialized() {
        return Uni.createFrom().completionStage(initialization);
    }

    private Uni<Void> initialize() {
        return Uni.createFrom().emitter(initializationEmitter -> {
            if (log.isTraceEnabled()) {
                log.trace("Initializing websocket with graphql-transport-ws protocol");
            }

            ObjectNode initNode = MAPPER.createObjectNode();
            initNode.put("type", "connection_init");
            if (!this.initPayload.isEmpty()) {
                initNode.set("payload", MAPPER.valueToTree(this.initPayload));
            }
            connectionInitMessage = initNode;

            ObjectNode pongNode = MAPPER.createObjectNode();
            pongNode.put("type", "pong");
            ObjectNode pongPayload = MAPPER.createObjectNode();
            pongPayload.put("message", "keepalive");
            pongNode.set("payload", pongPayload);
            pongMessage = pongNode;

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

            send(webSocket, connectionInitMessage);

            // set up a timeout for subscription initialization
            Cancellable timeoutWaitingForConnectionAckMessage = null;
            if (connectionInitializationTimeout != null) {
                timeoutWaitingForConnectionAckMessage = Uni.createFrom().item(1).onItem().delayIt()
                        .by(Duration.ofMillis(connectionInitializationTimeout))
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
                    ObjectNode message = parseIncomingMessage(text);
                    MessageType messageType = getMessageType(message);
                    switch (messageType) {
                        case PING:
                            send(webSocket, pongMessage);
                            break;
                        case CONNECTION_ACK:
                            // TODO: somehow protect against this being invoked multiple times?
                            if (finalTimeoutWaitingForConnectionAckMessage != null) {
                                finalTimeoutWaitingForConnectionAckMessage.cancel();
                            }
                            initializationEmitter.complete(null);
                            break;
                        case NEXT:
                            handleData(message.get("id").asText(), (ObjectNode) message.get("payload"));
                            break;
                        case ERROR:
                            handleOperationError(message.get("id").asText(), (ArrayNode) message.get("payload"));
                            break;
                        case COMPLETE:
                            handleComplete(message.get("id").asText());
                            break;
                        case CONNECTION_INIT:
                        case PONG:
                        case SUBSCRIBE:
                            break;
                    }
                } catch (IllegalArgumentException e) {
                    log.error("Unexpected message from server: " + text);
                    // should we fail the operations here?
                }
            });
        });
    }

    private void handleData(String operationId, ObjectNode data) {
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

    private void handleOperationError(String operationId, ArrayNode errors) {
        List<GraphQLError> parsedErrors = StreamSupport.stream(errors.spliterator(), false)
                .map(ResponseReader::readError)
                .collect(Collectors.toList());
        GraphQLClientException exception = new GraphQLClientException("Received an error", parsedErrors);
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

    @Override
    public String executeUni(ObjectNode request, UniEmitter<? super String> emitter) {
        String id = operationIdGenerator.generate();
        ensureInitialized().subscribe().with(ready -> {
            uniOperations.put(id, emitter);
            ObjectNode subscribe = createSubscribeMessage(request, id);
            send(webSocket, subscribe);
        }, emitter::fail);
        return id;
    }

    @Override
    public String executeMulti(ObjectNode request, MultiEmitter<? super String> emitter) {
        String id = operationIdGenerator.generate();
        ensureInitialized().subscribe().with(ready -> {
            multiOperations.put(id, emitter);
            ObjectNode subscribe = createSubscribeMessage(request, id);
            send(webSocket, subscribe);
        }, emitter::fail);
        return id;
    }

    @Override
    public void cancelUni(String id) {
        uniOperations.remove(id);
        send(webSocket, createCompleteMessage(id));
    }

    @Override
    public void cancelMulti(String id) {
        multiOperations.remove(id);
        send(webSocket, createCompleteMessage(id));
    }

    @Override
    public void close() {
        if (webSocket != null && !webSocket.isClosed()) {
            webSocket.close((short) 1000);
        }
    }

    private MessageType getMessageType(ObjectNode message) {
        return MessageType.fromString(message.get("type").asText());
    }

    private ObjectNode parseIncomingMessage(String message) {
        try {
            return (ObjectNode) MAPPER.readTree(message);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to parse incoming message", e);
        }
    }

    private ObjectNode createSubscribeMessage(ObjectNode request, String id) {
        ObjectNode payload = MAPPER.createObjectNode();

        payload.put("query", request.get("query").asText());
        JsonNode operationName = request.get("operationName");
        if (operationName != null && operationName.isTextual()) {
            payload.set("operationName", operationName);
        }
        JsonNode variables = request.get("variables");
        if (variables != null && variables.isObject()) {
            payload.set("variables", variables);
        }

        ObjectNode msg = MAPPER.createObjectNode();
        msg.put("type", "subscribe");
        msg.put("id", id);
        msg.set("payload", payload);
        return msg;
    }

    private ObjectNode createCompleteMessage(String id) {
        ObjectNode msg = MAPPER.createObjectNode();
        msg.put("type", "complete");
        msg.put("id", id);
        return msg;
    }

    private Uni<Void> send(WebSocket webSocket, ObjectNode message) {
        String string = message.toString();
        if (log.isTraceEnabled()) {
            log.trace(">>> " + string);
        }
        return Uni.createFrom().completionStage(webSocket.writeTextMessage(string).toCompletionStage());
    }

}
