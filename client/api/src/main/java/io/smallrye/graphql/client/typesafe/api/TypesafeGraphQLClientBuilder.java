package io.smallrye.graphql.client.typesafe.api;

import java.net.URI;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

import io.smallrye.graphql.client.websocket.WebsocketSubprotocol;

/**
 * Use this builder, when you are not in a CDI context, i.e. when working with Java SE.
 */
public interface TypesafeGraphQLClientBuilder {

    static TypesafeGraphQLClientBuilder newBuilder() {
        ServiceLoader<TypesafeGraphQLClientBuilder> loader = ServiceLoader.load(TypesafeGraphQLClientBuilder.class);
        Iterator<TypesafeGraphQLClientBuilder> iterator = loader.iterator();
        if (!iterator.hasNext())
            throw new ServiceConfigurationError("no " + TypesafeGraphQLClientBuilder.class.getName() + " in classpath");
        TypesafeGraphQLClientBuilder graphQlClientBuilder = iterator.next();
        if (iterator.hasNext())
            throw new ServiceConfigurationError(
                    "more than one " + TypesafeGraphQLClientBuilder.class.getName() + " in classpath");
        return graphQlClientBuilder;
    }

    /**
     * The base key used to read configuration values. Defaults to the fully qualified name of the API interface.
     */
    TypesafeGraphQLClientBuilder configKey(String configKey);

    /**
     * The URL where the GraphQL service is listening
     */
    default TypesafeGraphQLClientBuilder endpoint(String endpoint) {
        return endpoint(URI.create(endpoint));
    }

    /**
     * The URL where the GraphQL service is listening
     */
    TypesafeGraphQLClientBuilder endpoint(URI endpoint);

    /**
     * Path to the websocket endpoint. By default this is the regular HTTP endpoint with the protocol changed to `ws`.
     */
    TypesafeGraphQLClientBuilder websocketUrl(String url);

    /**
     * If this is true, then queries and mutations will also be executed over a websocket connection rather than over pure HTTP.
     * As this comes with higher overhead, it is false by default.
     */
    TypesafeGraphQLClientBuilder executeSingleOperationsOverWebsocket(boolean value);

    /**
     * Static headers to send with all methods in this client.
     *
     * @see Header
     * @see AuthorizationHeader
     */
    default TypesafeGraphQLClientBuilder headers(Map<String, String> headers) {
        if (headers != null) {
            headers.forEach(this::header);
        }
        return this;
    }

    /**
     * Static header to send with all methods in this client.
     *
     * @see Header
     * @see AuthorizationHeader
     */
    TypesafeGraphQLClientBuilder header(String name, String value);

    /**
     * Static payload to send with initialization method on subscription.
     */
    TypesafeGraphQLClientBuilder initPayload(Map<String, Object> initPayload);

    TypesafeGraphQLClientBuilder subprotocols(WebsocketSubprotocol... subprotocols);

    TypesafeGraphQLClientBuilder allowUnexpectedResponseFields(boolean value);

    /**
     * Maximum time (in milliseconds) allowed between initializing a subscription websocket and receiving
     * a subscription start acknowledgement from the server.
     */
    TypesafeGraphQLClientBuilder websocketInitializationTimeout(Integer timeoutInMilliseconds);

    /**
     * Build the proxy for that api interface.
     */
    <T> T build(Class<T> apiClass);

}
