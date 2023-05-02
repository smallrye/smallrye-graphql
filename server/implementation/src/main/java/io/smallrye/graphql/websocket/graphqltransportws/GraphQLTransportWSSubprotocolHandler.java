package io.smallrye.graphql.websocket.graphqltransportws;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;

import io.smallrye.graphql.execution.ExecutionResponse;
import io.smallrye.graphql.execution.ExecutionService;
import io.smallrye.graphql.websocket.AbstractGraphQLWebsocketHandler;
import io.smallrye.graphql.websocket.GraphQLWebSocketSession;

/**
 * Websocket subprotocol handler that implements the `graphql-transport-ws` subprotocol.
 */
public class GraphQLTransportWSSubprotocolHandler extends AbstractGraphQLWebsocketHandler {

    private final String pingMessage;
    private final String pongMessage;

    public GraphQLTransportWSSubprotocolHandler(GraphQLWebSocketSession session, Map<String, Object> context) {
        super(session, "next", context);
        this.pongMessage = createPongMessage().toString();
        this.pingMessage = createPingMessage().toString();
    }

    @Deprecated
    public GraphQLTransportWSSubprotocolHandler(GraphQLWebSocketSession session, ExecutionService executionService) {
        this(session, Collections.emptyMap());
    }

    @Override
    protected void onMessage(JsonObject message) {
        if (message != null) {
            MessageType messageType = getMessageType(message);
            try {
                switch (messageType) {
                    case CONNECTION_INIT:
                        if (message.get("payload") != null) {
                            getContext().put("init-payload",
                                    Collections.unmodifiableMap((Map<String, Object>) message.get("payload")));
                        }
                        sendConnectionAckMessage();
                        break;
                    case PING:
                        sendPongMessage();
                        break;
                    case PONG:
                        break;
                    case SUBSCRIBE:
                        onOperationRequest(message);
                        break;
                    case COMPLETE:
                        sendCancelMessage(message);
                        break;
                    case CONNECTION_ACK:
                    case NEXT:
                    case ERROR:
                        break;
                }
            } catch (IOException e) {
                LOG.warn(e);
            }
        }
    }

    private MessageType getMessageType(JsonObject message) {
        return MessageType.fromString(message.getString("type"));
    }

    @Override
    protected void closeDueToConnectionNotInitialized() {
        session.close((short) 4401, "Unauthorized");
    }

    @Override
    protected void sendErrorMessage(String operationId, ExecutionResponse executionResponse) throws IOException {
        session.sendMessage(createErrorMessage(operationId,
                executionResponse.getExecutionResultAsJsonObject().getJsonArray("errors")).toString());
    }

    private JsonObject createErrorMessage(String operationId, JsonArray errors) {
        return Json.createObjectBuilder()
                .add("id", operationId)
                .add("type", "error")
                .add("payload", errors)
                .build();
    }

    private void sendPongMessage() throws IOException {
        session.sendMessage(pongMessage);
    }

    @Override
    protected String getPingMessage() {
        return pingMessage;
    }

    private JsonObject createPongMessage() {
        return Json.createObjectBuilder()
                .add("type", "pong")
                .build();
    }

    private JsonObject createPingMessage() {
        return Json.createObjectBuilder()
                .add("type", "ping")
                .build();
    }

}
