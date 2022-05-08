package io.smallrye.graphql.websocket.graphqlws;

import java.io.IOException;

import jakarta.json.Json;
import jakarta.json.JsonObject;

import io.smallrye.graphql.execution.ExecutionResponse;
import io.smallrye.graphql.execution.ExecutionService;
import io.smallrye.graphql.websocket.AbstractGraphQLWebsocketHandler;
import io.smallrye.graphql.websocket.GraphQLWebSocketSession;

/**
 * Websocket subprotocol handler that implements the `graphql-ws` subprotocol.
 */
public class GraphQLWSSubprotocolHandler extends AbstractGraphQLWebsocketHandler {

    private final String pingMessage;

    public GraphQLWSSubprotocolHandler(GraphQLWebSocketSession session) {
        super(session, MessageType.GQL_DATA.asString());
        pingMessage = createPingMessage().toString();
    }

    @Deprecated
    public GraphQLWSSubprotocolHandler(GraphQLWebSocketSession session, ExecutionService executionService) {
        this(session);
    }

    @Override
    protected void onMessage(JsonObject message) {
        if (message != null) {
            MessageType messageType = getMessageType(message);
            try {
                switch (messageType) {
                    case GQL_CONNECTION_INIT:
                        sendConnectionAckMessage();
                        break;
                    case GQL_START:
                        sendDataMessage(message);
                        break;
                    case GQL_STOP:
                        sendCancelMessage(message);
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
        session.close((short) 4429, "Connection not initialized");
    }

    @Override
    protected String getPingMessage() {
        return pingMessage;
    }

    @Override
    protected void sendErrorMessage(String operationId, ExecutionResponse executionResponse) throws IOException {
        session.sendMessage(createErrorMessage(operationId,
                // TODO: the message should have a single error, but executionresult contains an array of errors? what do?
                executionResponse.getExecutionResultAsJsonObject().getJsonArray("errors").get(0)
                        .asJsonObject()).toString());
    }

    private JsonObject createErrorMessage(String operationId, JsonObject error) {
        return Json.createObjectBuilder()
                .add("id", operationId)
                .add("type", MessageType.GQL_ERROR.asString())
                .add("payload", error)
                .build();
    }

    private JsonObject createPingMessage() {
        return Json.createObjectBuilder()
                .add("type", MessageType.GQL_CONNECTION_KEEP_ALIVE.asString())
                .build();
    }

}
