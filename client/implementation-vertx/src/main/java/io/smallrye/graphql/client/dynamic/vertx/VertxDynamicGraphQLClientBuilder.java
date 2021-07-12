package io.smallrye.graphql.client.dynamic.vertx;

import io.smallrye.graphql.client.SmallRyeGraphQLClientMessages;
import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClient;
import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClientBuilder;
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
        if (url == null) {
            throw SmallRyeGraphQLClientMessages.msg.urlNotConfiguredForProgrammaticClient();
        }
        Vertx toUseVertx = vertx != null ? vertx : Vertx.vertx();
        return new VertxDynamicGraphQLClient(toUseVertx, url, headersMap, options);
    }

}
