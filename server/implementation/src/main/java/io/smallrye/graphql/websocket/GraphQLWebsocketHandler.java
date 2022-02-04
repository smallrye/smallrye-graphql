package io.smallrye.graphql.websocket;

public interface GraphQLWebsocketHandler {

    void onMessage(String message);

    void onThrowable(Throwable t);

    void onClose();

    void onEnd();

}
