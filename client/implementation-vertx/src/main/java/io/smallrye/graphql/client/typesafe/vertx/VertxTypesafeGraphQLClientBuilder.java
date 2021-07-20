package io.smallrye.graphql.client.typesafe.vertx;

import java.lang.reflect.Proxy;
import java.net.URI;
import java.security.AccessController;
import java.security.PrivilegedAction;

import javax.enterprise.inject.spi.CDI;

import io.smallrye.graphql.client.ErrorMessageProvider;
import io.smallrye.graphql.client.GraphQLClientConfiguration;
import io.smallrye.graphql.client.GraphQLClientsConfiguration;
import io.smallrye.graphql.client.typesafe.api.GraphQLClientApi;
import io.smallrye.graphql.client.typesafe.api.TypesafeGraphQLClientBuilder;
import io.smallrye.graphql.client.typesafe.impl.reflection.MethodInvocation;
import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;

public class VertxTypesafeGraphQLClientBuilder implements TypesafeGraphQLClientBuilder {
    private String configKey = null;
    private URI endpoint;
    private Vertx vertx;
    private WebClientOptions options;
    private WebClient webClient;

    @Override
    public TypesafeGraphQLClientBuilder configKey(String configKey) {
        this.configKey = configKey;
        return this;
    }

    public TypesafeGraphQLClientBuilder vertx(Vertx vertx) {
        this.vertx = vertx;
        return this;
    }

    public TypesafeGraphQLClientBuilder client(WebClient webClient) {
        this.webClient = webClient;
        return this;
    }

    public TypesafeGraphQLClientBuilder options(WebClientOptions options) {
        this.options = options;
        return this;
    }

    private Vertx vertx() {
        if (vertx == null)
            vertx = Vertx.vertx();
        return vertx;
    }

    @Override
    public TypesafeGraphQLClientBuilder endpoint(URI endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    @Override
    public <T> T build(Class<T> apiClass) {
        if (configKey == null) {
            configKey = configKey(apiClass);
        }

        GraphQLClientConfiguration persistentConfig;
        try {
            persistentConfig = CDI.current()
                    .select(GraphQLClientsConfiguration.class).get()
                    .getClients().get(configKey);
        } catch (IllegalStateException ex) {
            // Probably running in a non-CDI environment so we can't use the GraphQLClientsConfiguration bean
            // TODO: right now, the GraphQLClientsConfiguration contains only metadata derived
            // from config properties. Once we move annotation-derived metadata there, that bean will
            // always be necessary (otherwise the client would ignore annotations within GraphQLClientApi interfaces)
            // so we need to make sure that the config bean will be available without CDI
            // - perhaps fall back to a pure Java singleton if CDI is not available?
            persistentConfig = null;
        }
        if (persistentConfig != null) {
            applyConfig(persistentConfig);
        }
        if (endpoint == null) {
            throw ErrorMessageProvider.get().urlMissingErrorForNamedClient(configKey);
        }

        VertxTypesafeGraphQLClientProxy graphQlClient = new VertxTypesafeGraphQLClientProxy(vertx(), persistentConfig, options,
                endpoint, webClient);
        return apiClass.cast(Proxy.newProxyInstance(getClassLoader(apiClass), new Class<?>[] { apiClass },
                (proxy, method, args) -> invoke(apiClass, graphQlClient, method, args)));
    }

    private Object invoke(Class<?> apiClass, VertxTypesafeGraphQLClientProxy graphQlClient, java.lang.reflect.Method method,
            Object... args) {
        MethodInvocation methodInvocation = MethodInvocation.of(method, args);
        if (methodInvocation.isDeclaredInCloseable()) {
            graphQlClient.close();
            return null; // void
        }
        return graphQlClient.invoke(apiClass, methodInvocation);
    }

    private ClassLoader getClassLoader(Class<?> apiClass) {
        if (System.getSecurityManager() == null)
            return apiClass.getClassLoader();
        return AccessController.doPrivileged((PrivilegedAction<ClassLoader>) apiClass::getClassLoader);
    }

    /**
     * Applies values from known global configuration. This does NOT override values passed to this
     * builder by method calls.
     */
    private void applyConfig(GraphQLClientConfiguration configuration) {
        if (this.endpoint == null && configuration.getUrl() != null) {
            this.endpoint = URI.create(configuration.getUrl());
        }
    }

    private String configKey(Class<?> apiClass) {
        GraphQLClientApi annotation = apiClass.getAnnotation(GraphQLClientApi.class);
        if (annotation == null) {
            return apiClass.getName();
        }
        String keyFromAnnotation = annotation.configKey();
        return (keyFromAnnotation.isEmpty()) ? apiClass.getName() : keyFromAnnotation;
    }
}
