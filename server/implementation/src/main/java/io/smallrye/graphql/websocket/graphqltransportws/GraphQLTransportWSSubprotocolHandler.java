package io.smallrye.graphql.websocket.graphqltransportws;

import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.stream.JsonParsingException;

import org.jboss.logging.Logger;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import graphql.ExecutionResult;
import io.smallrye.graphql.execution.ExecutionResponse;
import io.smallrye.graphql.execution.ExecutionService;
import io.smallrye.graphql.websocket.GraphQLWebSocketSession;
import io.smallrye.graphql.websocket.GraphQLWebsocketHandler;
import io.smallrye.mutiny.infrastructure.Infrastructure;

/**
 * Websocket subprotocol handler that implements the `graphql-transport-ws` subprotocol.
 */
public class GraphQLTransportWSSubprotocolHandler implements GraphQLWebsocketHandler {

    // TODO: Replace with prepared log messages
    private final Logger LOG = Logger.getLogger(GraphQLTransportWSSubprotocolHandler.class.getName());

    private final GraphQLWebSocketSession session;
    private final ExecutionService executionService;

    private final AtomicBoolean connectionInitialized;

    private final String CONNECTION_ACK_MESSAGE;
    private final String PONG_MESSAGE;

    private final Map<String, Subscriber<ExecutionResult>> activeOperations;

    public GraphQLTransportWSSubprotocolHandler(GraphQLWebSocketSession session, ExecutionService executionService) {
        this.session = session;
        this.executionService = executionService;
        this.connectionInitialized = new AtomicBoolean(false);
        this.CONNECTION_ACK_MESSAGE = createConnectionAckMessage().toString();
        this.PONG_MESSAGE = createPongMessage().toString();
        this.activeOperations = new ConcurrentHashMap<>();
    }

    @Override
    public void onMessage(String text) {
        Infrastructure.getDefaultExecutor().execute(() -> {
            if (LOG.isTraceEnabled()) {
                LOG.trace("<<< " + text);
            }

            JsonObject message = null;
            MessageType messageType = null;
            try {
                message = parseIncomingMessage(text);
                messageType = getMessageType(message);
            } catch (JsonParsingException ex) {
                session.close((short) 4400, ex.getMessage());
                return;
            } catch (NullPointerException | IllegalArgumentException ex) {
                session.close((short) 4400, "Unknown message type");
                return;
            }
            try {
                switch (messageType) {
                    case CONNECTION_INIT:
                        if (connectionInitialized.getAndSet(true)) {
                            session.close((short) 4429, "Too many initialisation requests");
                        } else {
                            session.sendMessage(CONNECTION_ACK_MESSAGE);
                        }
                        break;
                    case PING:
                        session.sendMessage(PONG_MESSAGE);
                        break;
                    case PONG:
                        break;
                    case SUBSCRIBE:
                        if (!connectionInitialized.get()) {
                            session.close((short) 4401, "Unauthorized");
                            return;
                        }
                        String operationId = message.getString("id");
                        if (activeOperations.putIfAbsent(operationId, SINGLE_RESULT_MARKER) != null) {
                            session.close((short) 4409, "Subscriber for " + operationId + " already exists");
                            return;
                        }
                        JsonObject payload = message.getJsonObject("payload");
                        ExecutionResponse executionResponse = executionService.execute(payload);
                        ExecutionResult executionResult = executionResponse.getExecutionResult();
                        if (executionResult != null) {
                            if (!executionResult.isDataPresent()) {
                                // this means a validation error
                                session.sendMessage(createErrorMessage(operationId,
                                        executionResponse.getExecutionResultAsJsonObject().getJsonArray("errors")).toString());
                            } else {
                                Object data = executionResponse.getExecutionResult().getData();
                                if (data instanceof Map) {
                                    // this means the operation is a query or mutation
                                    // only send the response if the operation hasn't been cancelled
                                    if (activeOperations.remove(operationId) != null) {
                                        session.sendMessage(
                                                createNextMessage(operationId,
                                                        executionResponse.getExecutionResultAsJsonObject())
                                                                .toString());
                                        session.sendMessage(createCompleteMessage(operationId).toString());
                                    }
                                } else if (data instanceof Publisher) {
                                    // this means the operation is a subscription
                                    SubscriptionSubscriber subscriber = new SubscriptionSubscriber(session, operationId);
                                    Publisher<ExecutionResult> stream = executionResponse.getExecutionResult()
                                            .getData();
                                    if (stream != null) {
                                        // this is actually a subscription, so replace the `activeOperation` entry
                                        // with the actual subscriber
                                        activeOperations.put(operationId, subscriber);
                                        stream.subscribe(subscriber);
                                    }
                                } else {
                                    LOG.warn("Unknown execution result of type "
                                            + executionResponse.getExecutionResult().getData().getClass());
                                }
                            }
                        }
                        break;
                    case COMPLETE:
                        String opId = message.getString("id");
                        Subscriber<ExecutionResult> subscriber = activeOperations.remove(opId);
                        if (subscriber != null) {
                            if (subscriber instanceof SubscriptionSubscriber) {
                                ((SubscriptionSubscriber) subscriber).cancel();
                            }
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("Completed operation id " + opId + " per client's request");
                            }
                        } else {
                            if (LOG.isDebugEnabled()) {
                                LOG.debug(
                                        "Client requested to complete operation id " + opId
                                                + ", but no such operation is active");
                            }
                        }
                        break;
                    case CONNECTION_ACK:
                    case NEXT:
                    case ERROR:
                        break;
                }
            } catch (IOException e) {
                LOG.warn(e);
            }
        });
    }

    @Override
    public void onThrowable(Throwable t) {
        LOG.warn("Error in websocket", t);
    }

    @Override
    public void onClose() {
        LOG.debug("GraphQL-over-websocket session " + session + " closed");
        if (!session.isClosed()) {
            session.close((short) 1000, "");
        }
    }

    @Override
    public void onEnd() {
    }

    private MessageType getMessageType(JsonObject message) {
        return MessageType.fromString(message.getString("type"));
    }

    // TODO: we need more validation on the incoming messages (correct fields and types etc)
    private JsonObject parseIncomingMessage(String message) {
        return Json.createReader(new StringReader(message)).readObject();
    }

    private JsonObject createConnectionAckMessage() {
        return Json.createObjectBuilder()
                .add("type", "connection_ack")
                .build();
    }

    private JsonObject createNextMessage(String operationId, JsonObject payload) {
        return Json.createObjectBuilder()
                .add("type", "next")
                .add("id", operationId)
                .add("payload", payload)
                .build();
    }

    private JsonObject createCompleteMessage(String operationId) {
        return Json.createObjectBuilder()
                .add("type", "complete")
                .add("id", operationId)
                .build();
    }

    private JsonObject createPongMessage() {
        return Json.createObjectBuilder()
                .add("type", "pong")
                .build();
    }

    private JsonObject createErrorMessage(String operationId, JsonArray errors) {
        return Json.createObjectBuilder()
                .add("id", operationId)
                .add("type", "error")
                .add("payload", errors)
                .build();
    }

    /**
     * The middleman that subscribes to an execution result and forwards its events to the websocket channel.
     */
    private class SubscriptionSubscriber implements Subscriber<ExecutionResult> {

        private final AtomicReference<Subscription> subscription = new AtomicReference<>();
        private final GraphQLWebSocketSession session;
        private final String operationId;

        public SubscriptionSubscriber(GraphQLWebSocketSession session, String operationId) {
            this.session = session;
            this.operationId = operationId;
        }

        @Override
        public void onSubscribe(Subscription s) {
            subscription.set(s);
            subscription.get().request(1);
        }

        @Override
        public void onNext(ExecutionResult executionResult) {
            if (!session.isClosed()) {
                ExecutionResponse executionResponse = new ExecutionResponse(executionResult);
                try {
                    session.sendMessage(
                            createNextMessage(operationId,
                                    executionResponse.getExecutionResultAsJsonObject())
                                            .toString());
                } catch (IOException e) {
                    LOG.warn(e);
                }
                subscription.get().request(1);
            }
        }

        @Override
        public void onError(Throwable t) {
            // TODO: I'm not sure if/when this can happen. Even if the operation's root fails, it goes into `onNext`.
            t.printStackTrace();
        }

        @Override
        public void onComplete() {
            if (LOG.isTraceEnabled()) {
                LOG.trace("Subscription with id " + operationId + " completed");
            }
            try {
                session.sendMessage(createCompleteMessage(operationId).toString());
            } catch (IOException e) {
                LOG.warn(e);
            }
            activeOperations.remove(operationId);
        }

        public void cancel() {
            subscription.get().cancel();
        }
    }

    // dummy value to put into the `activeOperations` map for single-result operations
    private static final Subscriber<ExecutionResult> SINGLE_RESULT_MARKER = new Subscriber<ExecutionResult>() {
        @Override
        public void onSubscribe(Subscription s) {
        }

        @Override
        public void onNext(ExecutionResult executionResult) {
        }

        @Override
        public void onError(Throwable t) {
        }

        @Override
        public void onComplete() {
        }
    };

}
