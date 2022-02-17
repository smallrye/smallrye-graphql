package io.smallrye.graphql.client.vertx.typesafe;

import java.lang.reflect.Proxy;
import java.net.URI;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jboss.logging.Logger;

import io.smallrye.graphql.client.impl.ErrorMessageProvider;
import io.smallrye.graphql.client.impl.GraphQLClientConfiguration;
import io.smallrye.graphql.client.impl.GraphQLClientsConfiguration;
import io.smallrye.graphql.client.impl.typesafe.reflection.MethodInvocation;
import io.smallrye.graphql.client.typesafe.api.GraphQLClientApi;
import io.smallrye.graphql.client.typesafe.api.TypesafeGraphQLClientBuilder;
import io.smallrye.graphql.client.vertx.VertxClientOptionsHelper;
import io.smallrye.graphql.client.websocket.WebsocketSubprotocol;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;

public class VertxTypesafeGraphQLClientBuilder implements TypesafeGraphQLClientBuilder {
    private static Vertx VERTX;

    private static final Logger log = Logger.getLogger(VertxTypesafeGraphQLClientBuilder.class);

    private String configKey = null;
    private URI endpoint;
    private Map<String, String> headers;
    private List<WebsocketSubprotocol> subprotocols;
    private Vertx vertx;
    private HttpClientOptions options;
    private WebClient webClient;
    private HttpClient httpClient;
    private Integer subscriptionInitializationTimeout;

    public VertxTypesafeGraphQLClientBuilder() {
        this.subprotocols = new ArrayList<>();
    }

    @Override
    public VertxTypesafeGraphQLClientBuilder configKey(String configKey) {
        this.configKey = configKey;
        return this;
    }

    public VertxTypesafeGraphQLClientBuilder vertx(Vertx vertx) {
        this.vertx = vertx;
        return this;
    }

    public VertxTypesafeGraphQLClientBuilder client(WebClient webClient) {
        this.webClient = webClient;
        return this;
    }

    @SuppressWarnings("unused")
    public VertxTypesafeGraphQLClientBuilder options(HttpClientOptions options) {
        this.options = options;
        return this;
    }

    @Override
    public VertxTypesafeGraphQLClientBuilder endpoint(URI endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    @Override
    public VertxTypesafeGraphQLClientBuilder header(String name, String value) {
        if (this.headers == null) {
            this.headers = new LinkedHashMap<>();
        }
        this.headers.put(name, value);
        return this;
    }

    public VertxTypesafeGraphQLClientBuilder subprotocols(WebsocketSubprotocol... subprotocols) {
        this.subprotocols.addAll(Arrays.asList(subprotocols));
        return this;
    }

    @Override
    public VertxTypesafeGraphQLClientBuilder subscriptionInitializationTimeout(Integer timeoutInMilliseconds) {
        this.subscriptionInitializationTimeout = timeoutInMilliseconds;
        return this;
    }

    @Override
    public <T> T build(Class<T> apiClass) {
        if (this.options == null) {
            this.options = new WebClientOptions();
        }
        if (configKey == null) {
            configKey = configKey(apiClass);
        }

        applyConfigFor(apiClass);

        if (endpoint == null) {
            throw ErrorMessageProvider.get().urlMissingErrorForNamedClient(configKey);
        }

        initClients();
        if (subprotocols == null || subprotocols.isEmpty()) {
            subprotocols = new ArrayList<>(EnumSet.of(WebsocketSubprotocol.GRAPHQL_TRANSPORT_WS));
        }
        VertxTypesafeGraphQLClientProxy graphQlClient = new VertxTypesafeGraphQLClientProxy(apiClass, headers, endpoint,
                httpClient,
                webClient, subprotocols, subscriptionInitializationTimeout);
        return apiClass.cast(Proxy.newProxyInstance(getClassLoader(apiClass), new Class<?>[] { apiClass },
                (proxy, method, args) -> invoke(graphQlClient, method, args)));
    }

    private void applyConfigFor(Class<?> apiClass) {
        GraphQLClientsConfiguration configs = GraphQLClientsConfiguration.getInstance();
        // In case that we're running in a plain Java SE application, it is possible that the client configuration
        // hasn't been added yet, because there is no CDI extension or Jandex processor that scans for @GraphQLClientApi annotations
        // at startup => try adding a configuration entry dynamically for this client interface in particular.
        configs.initTypesafeClientApi(apiClass);

        GraphQLClientConfiguration persistentConfig = configs.getClient(configKey);
        if (persistentConfig != null) {
            applyConfig(persistentConfig);
        }
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

    private Object invoke(VertxTypesafeGraphQLClientProxy graphQlClient, java.lang.reflect.Method method,
            Object... args) {
        MethodInvocation methodInvocation = MethodInvocation.of(method, args);
        if (methodInvocation.isDeclaredInCloseable()) {
            graphQlClient.close();
            return null; // void
        }
        return graphQlClient.invoke(methodInvocation);
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
        if (this.headers == null && configuration.getHeaders() != null) {
            this.headers = configuration.getHeaders();
        }
        if (this.subscriptionInitializationTimeout == null && configuration.getSubscriptionInitializationTimeout() != null) {
            this.subscriptionInitializationTimeout = configuration.getSubscriptionInitializationTimeout();
        }
        if (configuration.getWebsocketSubprotocols() != null) {
            configuration.getWebsocketSubprotocols().forEach(protocol -> {
                try {
                    WebsocketSubprotocol e = WebsocketSubprotocol.fromString(protocol);
                    this.subprotocols.add(e);
                } catch (IllegalArgumentException e) {
                    log.warn(e);
                }
            });
        }

        VertxClientOptionsHelper.applyConfigToVertxOptions(options, configuration);
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
