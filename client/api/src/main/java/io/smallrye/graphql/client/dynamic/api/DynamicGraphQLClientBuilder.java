package io.smallrye.graphql.client.dynamic.api;

import java.util.Iterator;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

import io.smallrye.graphql.client.websocket.WebsocketSubprotocol;

public interface DynamicGraphQLClientBuilder {

    DynamicGraphQLClientBuilder url(String url);

    DynamicGraphQLClientBuilder websocketUrl(String url);

    DynamicGraphQLClientBuilder executeSingleOperationsOverWebsocket(boolean value);

    DynamicGraphQLClientBuilder configKey(String configKey);

    DynamicGraphQLClientBuilder header(String key, String value);

    DynamicGraphQLClientBuilder subprotocols(WebsocketSubprotocol... subprotocols);

    /**
     * Maximum time (in milliseconds) allowed between initializing a subscription websocket and receiving
     * a subscription start acknowledgement from the server.
     */
    DynamicGraphQLClientBuilder websocketInitializationTimeout(Integer timeoutInMilliseconds);

    DynamicGraphQLClient build();

    static DynamicGraphQLClientBuilder newBuilder() {
        ServiceLoader<DynamicGraphQLClientBuilder> loader = ServiceLoader.load(DynamicGraphQLClientBuilder.class);
        Iterator<DynamicGraphQLClientBuilder> iterator = loader.iterator();
        if (!iterator.hasNext())
            throw new ServiceConfigurationError("no " + DynamicGraphQLClientBuilder.class.getName() + " in classpath");
        DynamicGraphQLClientBuilder graphQlClientBuilder = iterator.next();
        if (iterator.hasNext())
            throw new ServiceConfigurationError(
                    "more than one " + DynamicGraphQLClientBuilder.class.getName() + " in classpath");
        return graphQlClientBuilder;
    }
}
