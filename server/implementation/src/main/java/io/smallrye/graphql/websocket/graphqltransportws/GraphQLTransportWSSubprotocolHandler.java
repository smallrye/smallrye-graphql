package io.smallrye.graphql.websocket.graphqltransportws;

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
 * Websocket subprotocol handler that implements the `graphql-transport-ws` subprotocol.
 */
public class GraphQLTransportWSSubprotocolHandler extends AbstractGraphQLWebsocketHandler {

    private final String pingMessage;
    private final String pongMessage;

    public GraphQLTransportWSSubprotocolHandler(GraphQLWebSocketSession session, Map<String, Object> context) {
        super(session, "next", context);
        this.pongMessage = writeValueAsString(NODE_FACTORY.objectNode().put("type", "pong"));
        this.pingMessage = writeValueAsString(NODE_FACTORY.objectNode().put("type", "ping"));
    }

    @Deprecated
    public GraphQLTransportWSSubprotocolHandler(GraphQLWebSocketSession session, ExecutionService executionService) {
        this(session, Collections.emptyMap());
    }

    @Override
    protected void onMessage(ObjectNode message) {
        if (message != null) {
            MessageType messageType = getMessageType(message);
            try {
                switch (messageType) {
                    case CONNECTION_INIT:
                        if (message.get("payload") != null) {
                            getContext().put("init-payload",
                                    Collections.unmodifiableMap((Map<String, Object>) OBJECT_MAPPER
                                            .treeToValue(message.get("payload"), Map.class)));
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

    private MessageType getMessageType(ObjectNode message) {
        return MessageType.fromString(message.get("type").asText());
    }

    @Override
    protected void closeDueToConnectionNotInitialized() {
        session.close((short) 4401, "Unauthorized");
    }

    @Override
    protected void sendErrorMessage(String operationId, ExecutionResponse executionResponse) throws IOException {
        ObjectNode result = executionResponse.getExecutionResultAsJsonObject();
        ArrayNode errors = (ArrayNode) result.get("errors");
        ObjectNode errorMessage = NODE_FACTORY.objectNode()
                .put("id", operationId)
                .put("type", "error");
        errorMessage.set("payload", errors);
        session.sendMessage(writeValueAsString(errorMessage));
    }

    private void sendPongMessage() throws IOException {
        session.sendMessage(pongMessage);
    }

    @Override
    protected String getPingMessage() {
        return pingMessage;
    }

}
