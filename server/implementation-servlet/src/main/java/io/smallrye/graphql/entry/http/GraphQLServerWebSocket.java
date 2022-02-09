package io.smallrye.graphql.entry.http;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.jboss.logging.Logger;

import io.smallrye.graphql.execution.ExecutionService;
import io.smallrye.graphql.websocket.GraphQLWebSocketSession;
import io.smallrye.graphql.websocket.GraphQLWebsocketHandler;
import io.smallrye.graphql.websocket.graphqltransportws.GraphQLTransportWSSubprotocolHandler;
import io.smallrye.graphql.websocket.graphqlws.GraphQLWSSubprotocolHandler;

/**
 * Executing GraphQL operations over a websocket.
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@ServerEndpoint(value = "/graphql", subprotocols = { "graphql-transport-ws", "graphql-ws" })
public class GraphQLServerWebSocket {

    private final Logger log = Logger.getLogger(GraphQLServerWebSocket.class.getName());

    private Map<Session, GraphQLWebsocketHandler> sessionsToHandlers = new ConcurrentHashMap<>();

    @Inject
    ExecutionService executionService;

    @OnOpen
    public void onOpen(Session session) {
        GraphQLWebsocketHandler handler = null;
        String subprotocol = session.getNegotiatedSubprotocol();
        switch (subprotocol) {
            case "graphql-transport-ws":
                handler = new GraphQLTransportWSSubprotocolHandler(new SmallRyeWebSocketSession(session),
                        executionService);
                break;
            case "graphql-ws":
                handler = new GraphQLWSSubprotocolHandler(new SmallRyeWebSocketSession(session),
                        executionService);
                break;
            default:
                log.warn("Unknown subprotocol: " + subprotocol);
                try {
                    session.close(
                            new CloseReason(CloseReason.CloseCodes.PROTOCOL_ERROR, "Unknown subprotocol: " + subprotocol));
                } catch (IOException e) {
                    log.warn(e);
                }
                return;
        }
        sessionsToHandlers.put(session, handler);
        log.debug("Opened graphql-over-websocket session on " + session + " with subprotocol=" + subprotocol);
    }

    @OnClose
    public void onClose(Session session) {
        log.debug("Closing session " + session);
        GraphQLWebsocketHandler handler = sessionsToHandlers.remove(session);
        if (handler != null) {
            handler.onClose();
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        GraphQLWebsocketHandler handler = sessionsToHandlers.remove(session);
        if (handler != null) {
            handler.onThrowable(throwable);
        }
    }

    @OnMessage
    public void handleMessage(Session session, String message) {
        GraphQLWebsocketHandler handler = sessionsToHandlers.get(session);
        if (handler != null) {
            handler.onMessage(message);
        } else {
            log.error("Unknown session: " + session);
        }
    }

    private class SmallRyeWebSocketSession implements GraphQLWebSocketSession {

        private final Session session;

        public SmallRyeWebSocketSession(Session session) {
            this.session = session;
        }

        @Override
        public void sendMessage(String message) throws IOException {
            if (log.isTraceEnabled()) {
                log.trace(">>> " + message);
            }
            session.getBasicRemote().sendText(message);
        }

        @Override
        public void close(short statusCode, String reason) {
            try {
                session.close(new CloseReason(CloseReason.CloseCodes.getCloseCode(statusCode), reason));
            } catch (IOException e) {
                log.warn(e);
            }
        }

        @Override
        public boolean isClosed() {
            return !session.isOpen();
        }

    }

}
