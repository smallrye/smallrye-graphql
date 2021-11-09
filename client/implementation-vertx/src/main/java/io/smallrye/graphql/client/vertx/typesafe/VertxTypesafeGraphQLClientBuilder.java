package io.smallrye.graphql.client.vertx.typesafe;

import java.lang.reflect.Proxy;
import java.net.URI;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;

import io.smallrye.graphql.client.impl.ErrorMessageProvider;
import io.smallrye.graphql.client.impl.GraphQLClientConfiguration;
import io.smallrye.graphql.client.impl.GraphQLClientsConfiguration;
import io.smallrye.graphql.client.impl.typesafe.reflection.MethodInvocation;
import io.smallrye.graphql.client.typesafe.api.GraphQLClientApi;
import io.smallrye.graphql.client.typesafe.api.TypesafeGraphQLClientBuilder;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.ext.web.client.WebClient;

public class VertxTypesafeGraphQLClientBuilder implements TypesafeGraphQLClientBuilder {
    private static Vertx VERTX;

    private String configKey = null;
    private URI endpoint;
    private Vertx vertx;
    private HttpClientOptions options;
    private WebClient webClient;
    private HttpClient httpClient;

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

    public TypesafeGraphQLClientBuilder options(HttpClientOptions options) {
        this.options = options;
        return this;
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

        GraphQLClientsConfiguration configs = GraphQLClientsConfiguration.getInstance();
        GraphQLClientConfiguration persistentConfig = configs
                .getClient(configKey);
        if (persistentConfig == null) {
            // in case that we're running in a plain Java SE application, it is possible that the client configuration
            // hasn't been added yet, because there is no CDI extension or Jandex processor that scans for @GraphQLClientApi annotations
            // at startup => try adding a configuration entry dynamically for this client interface in particular.
            // Then try again.
            configs.addTypesafeClientApis(Collections.singletonList(apiClass));
            persistentConfig = configs
                    .getClient(configKey);
        }
        if (persistentConfig != null) {
            applyConfig(persistentConfig);
        }

        if (endpoint == null) {
            throw ErrorMessageProvider.get().urlMissingErrorForNamedClient(configKey);
        }

        initClients();
        VertxTypesafeGraphQLClientProxy graphQlClient = new VertxTypesafeGraphQLClientProxy(persistentConfig,
                endpoint, httpClient, webClient);
        return apiClass.cast(Proxy.newProxyInstance(getClassLoader(apiClass), new Class<?>[] { apiClass },
                (proxy, method, args) -> invoke(apiClass, graphQlClient, method, args)));
    }

    private void initClients() {
        if (webClient == null) {
            if (httpClient == null) {
                httpClient = options != null ? vertx().createHttpClient(options) : vertx().createHttpClient();
            }
            webClient = WebClient.wrap(httpClient);
        } else {
            if (httpClient == null) {
                httpClient = options != null ? vertx().createHttpClient(options) : vertx().createHttpClient();
            }
        }
    }

    private Vertx vertx() {
        if (vertx == null) {
            Context vertxContext = Vertx.currentContext();
            if (vertxContext != null && vertxContext.owner() != null) {
                vertx = vertxContext.owner();
            } else {
                // create a new vertx instance if there is none
                if (VERTX == null) {
                    VERTX = Vertx.vertx();
                }
                vertx = VERTX;
            }
        }
        return vertx;
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
