package io.smallrye.graphql.client.vertx.typesafe;

import java.lang.reflect.Proxy;
import java.net.URI;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
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
import io.smallrye.graphql.client.vertx.VertxManager;
import io.smallrye.graphql.client.websocket.WebsocketSubprotocol;
import io.smallrye.mutiny.Uni;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;

public class VertxTypesafeGraphQLClientBuilder implements TypesafeGraphQLClientBuilder {

    private static final Logger log = Logger.getLogger(VertxTypesafeGraphQLClientBuilder.class);

    private String configKey = null;
    private URI endpoint;
    private String websocketUrl;
    private Boolean executeSingleOperationsOverWebsocket;
    private Map<String, String> headers;
    private Map<String, Uni<String>> dynamicHeaders;
    private Map<String, Object> initPayload;
    private List<WebsocketSubprotocol> subprotocols;
    private Vertx vertx;
    private HttpClientOptions options;
    private WebClient webClient;
    private HttpClient httpClient;
    private Integer websocketInitializationTimeout;
    private Boolean allowUnexpectedResponseFields;

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
    public TypesafeGraphQLClientBuilder websocketUrl(String url) {
        this.websocketUrl = url;
        return this;
    }

    @Override
    public TypesafeGraphQLClientBuilder executeSingleOperationsOverWebsocket(boolean value) {
        this.executeSingleOperationsOverWebsocket = value;
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

    @Override
    public TypesafeGraphQLClientBuilder dynamicHeader(String name, Uni<String> value) {
        if (this.dynamicHeaders == null) {
            this.dynamicHeaders = new LinkedHashMap<>();
        }
        this.dynamicHeaders.put(name, value);
        return this;
    }

    public VertxTypesafeGraphQLClientBuilder initPayload(Map<String, Object> initPayload) {
        if (this.initPayload == null) {
            this.initPayload = new LinkedHashMap<>();
        }
        this.initPayload.putAll(initPayload);
        return this;
    }

    public VertxTypesafeGraphQLClientBuilder subprotocols(WebsocketSubprotocol... subprotocols) {
        this.subprotocols.addAll(Arrays.asList(subprotocols));
        return this;
    }

    @Override
    public TypesafeGraphQLClientBuilder allowUnexpectedResponseFields(boolean value) {
        this.allowUnexpectedResponseFields = value;
        return this;
    }

    @Override
    public VertxTypesafeGraphQLClientBuilder websocketInitializationTimeout(Integer timeoutInMilliseconds) {
        this.websocketInitializationTimeout = timeoutInMilliseconds;
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
        if (websocketUrl == null) {
            websocketUrl = endpoint.toString().replaceFirst("http", "ws");
        }
        if (executeSingleOperationsOverWebsocket == null) {
            executeSingleOperationsOverWebsocket = false;
        }
        if (allowUnexpectedResponseFields == null) {
            allowUnexpectedResponseFields = false;
        }
        if (dynamicHeaders == null) {
            dynamicHeaders = new HashMap<>();
        }

        VertxTypesafeGraphQLClientProxy graphQlClient = new VertxTypesafeGraphQLClientProxy(apiClass, headers,
                dynamicHeaders, initPayload,
                endpoint,
                websocketUrl, executeSingleOperationsOverWebsocket, httpClient, webClient, subprotocols,
                websocketInitializationTimeout,
                allowUnexpectedResponseFields);
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
        return vertx != null ? vertx : VertxManager.get();
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
        if (this.websocketUrl == null && configuration.getWebsocketUrl() != null) {
            this.websocketUrl = configuration.getWebsocketUrl();
        }
        if (this.headers == null && configuration.getHeaders() != null) {
            this.headers = configuration.getHeaders();
        }
        if (this.dynamicHeaders == null && configuration.getDynamicHeaders() != null) {
            this.dynamicHeaders = configuration.getDynamicHeaders();
        }
        if (this.initPayload == null && configuration.getInitPayload() != null) {
            this.initPayload = configuration.getInitPayload();
        }
        if (this.websocketInitializationTimeout == null && configuration.getWebsocketInitializationTimeout() != null) {
            this.websocketInitializationTimeout = configuration.getWebsocketInitializationTimeout();
        }
        if (executeSingleOperationsOverWebsocket == null && configuration.getExecuteSingleOperationsOverWebsocket() != null) {
            this.executeSingleOperationsOverWebsocket = configuration.getExecuteSingleOperationsOverWebsocket();
        }
        if (allowUnexpectedResponseFields == null && configuration.getAllowUnexpectedResponseFields() != null) {
            this.allowUnexpectedResponseFields = configuration.getAllowUnexpectedResponseFields();
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
