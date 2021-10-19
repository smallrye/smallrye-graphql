package io.smallrye.graphql.client.dynamic.vertx;

import io.smallrye.graphql.client.ErrorMessageProvider;
import io.smallrye.graphql.client.GraphQLClientConfiguration;
import io.smallrye.graphql.client.GraphQLClientsConfiguration;
import io.smallrye.graphql.client.SmallRyeGraphQLClientMessages;
import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClient;
import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClientBuilder;
import io.vertx.core.Context;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.http.impl.headers.HeadersMultiMap;
import io.vertx.ext.web.client.WebClientOptions;

/**
 * Implementation of dynamic client builder that creates GraphQL clients using Vert.x under the hood.
 */
public class VertxDynamicGraphQLClientBuilder implements DynamicGraphQLClientBuilder {

    private Vertx vertx;
    private String url;
    private String configKey;
    private final MultiMap headersMap;
    private WebClientOptions options;

    public VertxDynamicGraphQLClientBuilder() {
        headersMap = new HeadersMultiMap();
        headersMap.set("Content-Type", "application/json");
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

    @Override
    public DynamicGraphQLClientBuilder url(String url) {
        this.url = url;
        return this;
    }

    @Override
    public DynamicGraphQLClientBuilder configKey(String configKey) {
        this.configKey = configKey;
        return this;
    }

    @Override
    public DynamicGraphQLClient build() {
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
        return new VertxDynamicGraphQLClient(toUseVertx, url, headersMap, options);
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
    }

}
