package io.smallrye.graphql.websocket.graphqlws;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import io.smallrye.graphql.execution.ExecutionResponse;
import io.smallrye.graphql.execution.ExecutionService;
import io.smallrye.graphql.websocket.AbstractGraphQLWebsocketHandler;
import io.smallrye.graphql.websocket.GraphQLWebSocketSession;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Websocket subprotocol handler that implements the `graphql-ws` subprotocol.
 */
public class GraphQLWSSubprotocolHandler extends AbstractGraphQLWebsocketHandler {

    private final String pingMessage;

    public GraphQLWSSubprotocolHandler(GraphQLWebSocketSession session, Map<String, Object> context) {
        super(session, MessageType.GQL_DATA.asString(), context);
        pingMessage = writeValueAsString(
                NODE_FACTORY.objectNode().put("type", MessageType.GQL_CONNECTION_KEEP_ALIVE.asString()));
    }

    @Deprecated
    public GraphQLWSSubprotocolHandler(GraphQLWebSocketSession session, ExecutionService executionService) {
        this(session, Collections.emptyMap());
    }

    @Override
    protected void onMessage(ObjectNode message) {
        if (message != null) {
            MessageType messageType = getMessageType(message);
            try {
                switch (messageType) {
                    case GQL_CONNECTION_INIT:
                        if (message.get("payload") != null) {
                            getContext().put("init-payload",
                                    Collections.unmodifiableMap((Map<String, Object>) OBJECT_MAPPER
                                            .treeToValue(message.get("payload"), Map.class)));
                        }
                        sendConnectionAckMessage();
                        break;
                    case GQL_START:
                        onOperationRequest(message);
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

    private MessageType getMessageType(ObjectNode message) {
        return MessageType.fromString(message.get("type").asText());
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
        ObjectNode result = executionResponse.getExecutionResultAsJsonObject();
        ArrayNode errors = (ArrayNode) result.get("errors");
        // TODO: the message should have a single error, but executionresult contains an array of errors? what do?
        ObjectNode error = (ObjectNode) errors.get(0);
        ObjectNode errorMessage = NODE_FACTORY.objectNode()
                .put("id", operationId)
                .put("type", MessageType.GQL_ERROR.asString());
        errorMessage.set("payload", error);
        session.sendMessage(writeValueAsString(errorMessage));
    }

}
