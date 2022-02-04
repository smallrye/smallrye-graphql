package io.smallrye.graphql.websocket;

import java.io.IOException;

/**
 * This is a simple abstraction over a websocket session to be able to abstract away from the underlying API.
 * The reason is to be able to implement protocol handlers which will work with Vert.x websockets as well as
 * JSR-356 endpoints.
 */
public interface GraphQLWebSocketSession {

    void sendMessage(String message) throws IOException;

    void close(short statusCode, String reason);

    boolean isClosed();

}
