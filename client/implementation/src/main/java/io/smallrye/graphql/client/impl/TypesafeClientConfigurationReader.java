package io.smallrye.graphql.client.impl;

import static io.smallrye.graphql.client.impl.GraphQLClientsConfiguration.getConfiguredHeaders;

import java.util.HashMap;
import java.util.Optional;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

import io.smallrye.graphql.client.typesafe.api.GraphQLClientApi;

/**
 * Parses a typesafe client's configuration object from available config properties and annotations.
 */
class TypesafeClientConfigurationReader {

    private final GraphQLClientApi annotation;
    private final Config mpConfig;

    private final String configKey;
    private final GraphQLClientConfiguration clientConfiguration = new GraphQLClientConfiguration();

    TypesafeClientConfigurationReader(Class<?> apiClass) {
        this.annotation = apiClass.getAnnotation(GraphQLClientApi.class);
        this.configKey = (annotation != null && !annotation.configKey().isEmpty()) ? annotation.configKey()
                : apiClass.getName();

        // Now, read configuration from config properties.
        // These take precedence over values in the GraphQLClientApi annotation
        this.mpConfig = ConfigProvider.getConfig();

        buildUrl();
        buildHeaders();
    }

    private void buildUrl() {
        Optional<String> configuredUrl = mpConfig.getOptionalValue(configKey + "/mp-graphql/url", String.class);
        if (configuredUrl.isPresent()) {
            clientConfiguration.setUrl(configuredUrl.get());
        } else if (annotation != null && !annotation.endpoint().isEmpty()) {
            clientConfiguration.setUrl(annotation.endpoint());
        }
    }

    private void buildHeaders() {
        clientConfiguration.setHeaders(getConfiguredHeaders(configKey, mpConfig));
        clientConfiguration.setDynamicHeaders(new HashMap<>());
    }

    GraphQLClientConfiguration getClientConfiguration() {
        return clientConfiguration;
    }

    String getConfigKey() {
        return configKey;
    }
}
