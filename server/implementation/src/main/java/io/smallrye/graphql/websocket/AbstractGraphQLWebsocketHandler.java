package io.smallrye.graphql.websocket;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.jboss.logging.Logger;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import graphql.ExecutionResult;
import graphql.ExecutionResultImpl;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import io.smallrye.graphql.execution.ExecutionResponse;
import io.smallrye.graphql.execution.ExecutionResponseWriter;
import io.smallrye.graphql.execution.ExecutionService;
import io.smallrye.graphql.spi.LookupService;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.subscription.Cancellable;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

public abstract class AbstractGraphQLWebsocketHandler implements GraphQLWebsocketHandler {
    // TODO: Replace with prepared log messages
    protected static final Logger LOG = Logger.getLogger(GraphQLWebsocketHandler.class.getName());
    protected static final ObjectMapper OBJECT_MAPPER = JsonMapper.builder().build();
    protected static final JsonNodeFactory NODE_FACTORY = JsonNodeFactory.instance;

    protected final ExecutionService executionService = LookupService.get().getInstance(ExecutionService.class).get();
    protected final GraphQLWebSocketSession session;
    protected final AtomicBoolean connectionInitialized;
    protected final String connectionAckMessage;
    protected final Map<String, Subscriber<ExecutionResult>> activeOperations;
    protected final Cancellable keepAliveSender;
    private final String dataMessageTypeName;
    private final Map<String, Object> context;

    public AbstractGraphQLWebsocketHandler(GraphQLWebSocketSession session, String dataMessageTypeName,
            Map<String, Object> context) {
        this.session = session;
        this.dataMessageTypeName = dataMessageTypeName;
        this.context = context;
        this.connectionInitialized = new AtomicBoolean(false);
        this.connectionAckMessage = writeValueAsString(createConnectionAckMessage());
        this.activeOperations = new ConcurrentHashMap<>();
        this.keepAliveSender = Multi.createFrom().ticks()
                .startingAfter(Duration.ofSeconds(10))
                .every(Duration.ofSeconds(10))
                .subscribe().with(tick -> sendKeepAlive());
    }

    @Override
    public void onMessage(String text) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("<<< " + text);
        }
        onMessage(getMessageAsObjectNode(text));
    }

    @Override
    public void onThrowable(Throwable t) {
        LOG.warn("Error in websocket", t);
        if (keepAliveSender != null) {
            keepAliveSender.cancel();
        }
    }

    @Override
    public void onClose() {
        LOG.debug("GraphQL-over-websocket session " + session + " closed");
        activeOperations.forEach((id, operation) -> cancelOperation(id));
        if (!session.isClosed()) {
            session.close((short) 1000, "");
        }
        if (keepAliveSender != null) {
            keepAliveSender.cancel();
        }
    }

    @Override
    public void onEnd() {
    }

    protected void sendConnectionAckMessage() throws IOException {
        if (connectionInitialized.getAndSet(true)) {
            session.close((short) 4429, "Too many initialisation requests");
        } else {
            session.sendMessage(connectionAckMessage);
        }
    }

    protected void onOperationRequest(ObjectNode message) {
        String operationId = message.get("id").asText();
        if (validSubscription(operationId)) {
            ObjectNode payload = (ObjectNode) message.get("payload");
            executionService.executeAsync(payload, context, new ExecutionResponseWriter() {

                @Override
                public void fail(Throwable t) {
                    LOG.warn("Cannot execute GraphQL operation", t);

                    GraphQLError error = GraphqlErrorBuilder
                            .newError()
                            .message("Internal server error")
                            .build();
                    ExecutionResult executionResult = ExecutionResultImpl
                            .newExecutionResult()
                            .addError(error)
                            .build();
                    ExecutionResponse executionResponse = new ExecutionResponse(executionResult);
                    try {
                        sendErrorMessage(operationId, executionResponse);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public void write(ExecutionResponse executionResponse) {
                    ExecutionResult executionResult = executionResponse.getExecutionResult();
                    if (executionResult != null) {
                        try {
                            if (!executionResult.isDataPresent()) {
                                sendErrorMessage(operationId, executionResponse);
                            } else {
                                Object data = executionResponse.getExecutionResult().getData();
                                if (data instanceof Map) {
                                    sendSingleMessage(operationId, executionResponse);
                                } else if (data instanceof Publisher) {
                                    sendStreamingMessage(operationId, executionResponse);
                                } else if (data == null) {
                                    sendErrorMessage(operationId, executionResponse);
                                } else {
                                    logUnknownResult(executionResult);
                                }
                            }
                        } catch (IOException ioe) {
                            fail(ioe);
                        }
                    }
                }
            });
        }
    }

    private ObjectNode createConnectionAckMessage() {
        return NODE_FACTORY.objectNode()
                .put("type", "connection_ack");
    }

    private ObjectNode getMessageAsObjectNode(String text) {
        try {
            return parseIncomingMessage(text);
        } catch (JacksonException ex) {
            session.close((short) 4400, ex.getMessage());
            return null;
        } catch (NullPointerException | IllegalArgumentException ex) {
            session.close((short) 4400, "Unknown message type");
            return null;
        }
    }

    // TODO: we need more validation on the incoming messages (correct fields and types etc)
    private ObjectNode parseIncomingMessage(String message) {
        return (ObjectNode) OBJECT_MAPPER.readTree(message);
    }

    private ObjectNode createCompleteMessage(String operationId) {
        return NODE_FACTORY.objectNode()
                .put("type", "complete")
                .put("id", operationId);
    }

    private ObjectNode createDataMessage(String operationId, ObjectNode payload) {
        ObjectNode msg = NODE_FACTORY.objectNode()
                .put("type", this.dataMessageTypeName)
                .put("id", operationId);
        msg.set("payload", payload);
        return msg;
    }

    private void logUnknownResult(ExecutionResult executionResult) {
        LOG.warn("Unknown data type of execution result: "
                + executionResult.getData().getClass());
    }

    private void sendSingleMessage(String operationId, ExecutionResponse executionResponse) throws IOException {
        if (activeOperations.remove(operationId) != null) {
            session.sendMessage(
                    writeValueAsString(createDataMessage(operationId,
                            executionResponse.getExecutionResultAsJsonObject())));
            session.sendMessage(writeValueAsString(createCompleteMessage(operationId)));
        }
    }

    private void sendStreamingMessage(String operationId, ExecutionResponse executionResponse) {
        SubscriptionSubscriber subscriber = new SubscriptionSubscriber(session, operationId);
        Publisher<ExecutionResult> stream = executionResponse.getExecutionResult()
                .getData();
        if (stream != null) {
            activeOperations.put(operationId, subscriber);
            stream.subscribe(subscriber);
        }

    }

    private void sendKeepAlive() {
        try {
            session.sendMessage(getPingMessage());
        } catch (IOException e) {
            LOG.warn(e);
        }
    }

    protected void sendCancelMessage(ObjectNode message) {
        String opId = message.get("id").asText();
        boolean cancelled = cancelOperation(opId);
        if (cancelled) {
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
    }

    private boolean cancelOperation(String opId) {
        Subscriber<ExecutionResult> subscriber = activeOperations.remove(opId);
        if (subscriber != null) {
            if (subscriber instanceof SubscriptionSubscriber) {
                ((SubscriptionSubscriber) subscriber).cancel();
            }
            return true;
        } else {
            return false;
        }
    }

    private boolean validSubscription(String operationId) {
        if (!connectionInitialized.get()) {
            closeDueToConnectionNotInitialized();
            return false;
        }
        if (activeOperations.putIfAbsent(operationId, SINGLE_RESULT_MARKER) != null) {
            session.close((short) 4409, "Subscriber for " + operationId + " already exists");
            return false;
        }
        return true;
    }

    protected static String writeValueAsString(ObjectNode node) {
        try {
            return OBJECT_MAPPER.writeValueAsString(node);
        } catch (JacksonException e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract void onMessage(ObjectNode message);

    protected abstract void sendErrorMessage(String operationId, ExecutionResponse executionResponse) throws IOException;

    protected abstract void closeDueToConnectionNotInitialized();

    protected abstract String getPingMessage();

    public Map<String, Object> getContext() {
        return context;
    }

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
                            writeValueAsString(createDataMessage(operationId,
                                    executionResponse.getExecutionResultAsJsonObject())));
                } catch (IOException e) {
                    LOG.warn(e);
                }
                subscription.get().request(1);
            }
        }

        @Override
        public void onError(Throwable t) {
            t.printStackTrace();
        }

        @Override
        public void onComplete() {
            if (LOG.isTraceEnabled()) {
                LOG.trace("Subscription with id " + operationId + " completed");
            }
            try {
                session.sendMessage(writeValueAsString(createCompleteMessage(operationId)));
            } catch (IOException e) {
                LOG.warn(e);
            }
            activeOperations.remove(operationId);
        }

        public void cancel() {
            Subscription sub = subscription.get();
            if (sub != null) {
                sub.cancel();
            }
        }
    }

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
