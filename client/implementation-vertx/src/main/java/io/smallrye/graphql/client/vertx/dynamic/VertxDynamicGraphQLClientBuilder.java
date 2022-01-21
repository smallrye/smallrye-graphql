package io.smallrye.graphql.client.vertx.dynamic;

import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jboss.logging.Logger;

import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClient;
import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClientBuilder;
import io.smallrye.graphql.client.impl.ErrorMessageProvider;
import io.smallrye.graphql.client.impl.GraphQLClientConfiguration;
import io.smallrye.graphql.client.impl.GraphQLClientsConfiguration;
import io.smallrye.graphql.client.impl.SmallRyeGraphQLClientMessages;
import io.smallrye.graphql.client.vertx.ssl.SSLTools;
import io.smallrye.graphql.client.websocket.WebsocketSubprotocol;
import io.vertx.core.Context;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.http.impl.headers.HeadersMultiMap;
import io.vertx.core.net.JksOptions;
import io.vertx.ext.web.client.WebClientOptions;

/**
 * Implementation of dynamic client builder that creates GraphQL clients using Vert.x under the hood.
 */
public class VertxDynamicGraphQLClientBuilder implements DynamicGraphQLClientBuilder {

    private static final Logger log = Logger.getLogger(VertxDynamicGraphQLClientBuilder.class);

    private Vertx vertx;
    private String url;
    private String configKey;
    private final MultiMap headersMap;
    private WebClientOptions options;
    private List<WebsocketSubprotocol> subprotocols;

    public VertxDynamicGraphQLClientBuilder() {
        headersMap = new HeadersMultiMap();
        headersMap.set("Content-Type", "application/json");
        subprotocols = new ArrayList<>();
    }

    public VertxDynamicGraphQLClientBuilder vertx(Vertx vertx) {
        this.vertx = vertx;
        return this;
    }

    public VertxDynamicGraphQLClientBuilder header(String name, String value) {
        headersMap.set(name, value);
        return this;
    }

    public VertxDynamicGraphQLClientBuilder options(WebClientOptions options) {
        this.options = options;
        return this;
    }

    public VertxDynamicGraphQLClientBuilder subprotocols(WebsocketSubprotocol... subprotocols) {
        this.subprotocols.addAll(Arrays.asList(subprotocols));
        return this;
    }

    @Override
    public VertxDynamicGraphQLClientBuilder url(String url) {
        this.url = url;
        return this;
    }

    @Override
    public VertxDynamicGraphQLClientBuilder configKey(String configKey) {
        this.configKey = configKey;
        return this;
    }

    @Override
    public DynamicGraphQLClient build() {
        if (this.options == null) {
            this.options = new WebClientOptions();
        }
        if (configKey != null) {
            GraphQLClientConfiguration persistentConfig = GraphQLClientsConfiguration.getInstance().getClient(configKey);
            if (persistentConfig != null) {
                applyConfig(persistentConfig);
            }
        }
        if (url == null) {
            if (configKey == null) {
                throw SmallRyeGraphQLClientMessages.msg.urlNotConfiguredForProgrammaticClient();
            } else {
                throw ErrorMessageProvider.get().urlMissingErrorForNamedClient(configKey);
            }
        }
        Vertx toUseVertx;
        if (vertx != null) {
            toUseVertx = vertx;
        } else {
            Context vertxContext = Vertx.currentContext();
            if (vertxContext != null && vertxContext.owner() != null) {
                toUseVertx = vertxContext.owner();
            } else {
                // create a new vertx instance if there is none
                toUseVertx = Vertx.vertx();
            }
        }
        return new VertxDynamicGraphQLClient(toUseVertx, url, headersMap, options, subprotocols);
    }

    /**
     * Applies values from known global configuration. This does NOT override values passed to this
     * builder by method calls.
     */
    private void applyConfig(GraphQLClientConfiguration configuration) {
        if (this.url == null && configuration.getUrl() != null) {
            this.url = configuration.getUrl();
        }
        configuration.getHeaders().forEach((k, v) -> {
            if (!this.headersMap.contains(k)) {
                this.headersMap.set(k, v);
            }
        });
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

        if (options.getTrustStoreOptions() == null && configuration.getTrustStore() != null) {
            options.setSsl(true);
            JksOptions trustStoreOptions = new JksOptions();
            KeyStore trustStore = SSLTools.createKeyStore(configuration.getTrustStore(),
                    configuration.getTrustStoreType(),
                    configuration.getTrustStorePassword());
            trustStoreOptions.setValue(SSLTools.asBuffer(trustStore, configuration.getTrustStorePassword().toCharArray()));
            trustStoreOptions.setPassword(new String(configuration.getTrustStorePassword()));
            options.setTrustStoreOptions(trustStoreOptions);
        }

        if (options.getKeyStoreOptions() == null && configuration.getKeyStore() != null) {
            options.setSsl(true);
            JksOptions keyStoreOptions = new JksOptions();
            KeyStore keyStore = SSLTools.createKeyStore(configuration.getKeyStore(),
                    configuration.getKeyStoreType(),
                    configuration.getKeyStorePassword());
            keyStoreOptions.setValue(SSLTools.asBuffer(keyStore, configuration.getKeyStorePassword().toCharArray()));
            keyStoreOptions.setPassword(new String(configuration.getKeyStorePassword()));
            options.setKeyStoreOptions(keyStoreOptions);
        }

        if (options.isSsl()) {
            // TODO: this is not supported yet
            options.setVerifyHost(false);
        }
    }

}
