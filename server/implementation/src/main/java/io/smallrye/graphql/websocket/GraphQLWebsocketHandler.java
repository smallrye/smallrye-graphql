package io.smallrye.graphql.websocket;

public interface GraphQLWebsocketHandler {

    /**
     * Called when a message arrives and needs to be handled. Implementation of this method MUST NOT block the calling thread!
     */
    void onMessage(String message);

    void onThrowable(Throwable t);

    void onClose();

    void onEnd();

}
