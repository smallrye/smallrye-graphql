package io.smallrye.graphql.websocket;

import java.io.IOException;
import java.io.StringReader;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.stream.JsonParsingException;

import org.jboss.logging.Logger;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import graphql.ExecutionResult;
import io.smallrye.graphql.execution.ExecutionResponse;
import io.smallrye.graphql.execution.ExecutionResponseWriter;
import io.smallrye.graphql.execution.ExecutionService;
import io.smallrye.graphql.spi.LookupService;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.subscription.Cancellable;

public abstract class AbstractGraphQLWebsocketHandler implements GraphQLWebsocketHandler {
    // TODO: Replace with prepared log messages
    protected final Logger LOG = Logger.getLogger(GraphQLWebsocketHandler.class.getName());

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
        this.connectionAckMessage = createConnectionAckMessage().toString();
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
        onMessage(getMessageAsJsonObject(text));
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

    protected void sendDataMessage(JsonObject message) {
        String operationId = message.getString("id");
        if (validSubscription(operationId)) {
            JsonObject payload = message.getJsonObject("payload");

            executionService.executeAsync(payload, context, new ExecutionResponseWriter() {
                @Override
                public void write(ExecutionResponse executionResponse) {
                    ExecutionResult executionResult = executionResponse.getExecutionResult();
                    if (executionResult != null) {
                        try {
                            if (!executionResult.isDataPresent()) {
                                // this means a validation error
                                sendErrorMessage(operationId, executionResponse);
                            } else {
                                Object data = executionResponse.getExecutionResult().getData();
                                if (data instanceof Map) {
                                    // this means the operation is a query or mutation
                                    // only send the response if the operation hasn't been cancelled
                                    sendSingleMessage(operationId, executionResponse);
                                } else if (data instanceof Publisher) {
                                    // this means the operation is a subscription
                                    sendStreamingMessage(operationId, executionResponse);
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

    private JsonObject createConnectionAckMessage() {
        return Json.createObjectBuilder()
                .add("type", "connection_ack")
                .build();
    }

    private JsonObject getMessageAsJsonObject(String text) {
        try {
            return parseIncomingMessage(text);
        } catch (JsonParsingException ex) {
            session.close((short) 4400, ex.getMessage());
            return null;
        } catch (NullPointerException | IllegalArgumentException ex) {
            session.close((short) 4400, "Unknown message type");
            return null;
        }
    }

    // TODO: we need more validation on the incoming messages (correct fields and types etc)
    private JsonObject parseIncomingMessage(String message) {
        return Json.createReader(new StringReader(message)).readObject();
    }

    private JsonObject createCompleteMessage(String operationId) {
        return Json.createObjectBuilder()
                .add("type", "complete")
                .add("id", operationId)
                .build();
    }

    private JsonObject createDataMessage(String operationId, JsonObject payload) {
        return Json.createObjectBuilder()
                .add("type", this.dataMessageTypeName)
                .add("id", operationId)
                .add("payload", payload)
                .build();
    }

    private void logUnknownResult(ExecutionResult executionResult) {
        LOG.warn("Unknown execution result of type "
                + executionResult.getClass());
    }

    private void sendSingleMessage(String operationId, ExecutionResponse executionResponse) throws IOException {
        if (activeOperations.remove(operationId) != null) {
            session.sendMessage(
                    createDataMessage(operationId,
                            executionResponse.getExecutionResultAsJsonObject())
                                    .toString());
            session.sendMessage(createCompleteMessage(operationId).toString());
        }
    }

    private void sendStreamingMessage(String operationId, ExecutionResponse executionResponse) {
        SubscriptionSubscriber subscriber = new SubscriptionSubscriber(session, operationId);
        Publisher<ExecutionResult> stream = executionResponse.getExecutionResult()
                .getData();
        if (stream != null) {
            // this is actually a subscription, so replace the `activeOperation` entry
            // with the actual subscriber
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

    protected void sendCancelMessage(JsonObject message) {
        String opId = message.getString("id");
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

    // cancel the operation with this id, returns true if it actually cancels an operation,
    // false if no such operation is active
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

    protected abstract void onMessage(JsonObject message);

    protected abstract void sendErrorMessage(String operationId, ExecutionResponse executionResponse) throws IOException;

    protected abstract void closeDueToConnectionNotInitialized();

    protected abstract String getPingMessage();

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
                            createDataMessage(operationId,
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
            Subscription sub = subscription.get();
            if (sub != null) {
                sub.cancel();
            }
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
