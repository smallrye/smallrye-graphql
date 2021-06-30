package io.smallrye.graphql.client;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

import io.smallrye.graphql.client.typesafe.api.GraphQLClientApi;

/**
 * Parses a typesafe client's configuration object from available config properties and annotations.
 */
class TypesafeClientConfigurationReader {

    private final String configKey;
    private final GraphQLClientConfiguration clientConfiguration;

    TypesafeClientConfigurationReader(Class<?> apiClass) {
        GraphQLClientApi annotation = apiClass.getAnnotation(GraphQLClientApi.class);
        if (annotation == null) {
            throw new RuntimeException("Could not find a GraphQLClientApi annotation on " + apiClass);
        }
        configKey = !annotation.configKey().isEmpty() ? annotation.configKey() : apiClass.getName();
        clientConfiguration = new GraphQLClientConfiguration();

        // Now, read configuration from config properties.
        // These take precedence over values in the GraphQLClientApi annotation
        Config mpConfig = ConfigProvider.getConfig();

        // URL
        Optional<String> configuredUrl = mpConfig.getOptionalValue(configKey + "/mp-graphql/url", String.class);
        if (configuredUrl.isPresent()) {
            clientConfiguration.setUrl(configuredUrl.get());
        } else {
            String endpointFromAnnotation = annotation.endpoint();
            if (!endpointFromAnnotation.isEmpty()) {
                clientConfiguration.setUrl(endpointFromAnnotation);
            }
        }

        // Headers
        clientConfiguration.setHeaders(getConfigurationValueMap(configKey, "header", mpConfig));
    }

    GraphQLClientConfiguration getClientConfiguration() {
        return clientConfiguration;
    }

    String getConfigKey() {
        return configKey;
    }

    private Map<String, String> getConfigurationValueMap(String clientName, String configKey, Config config) {
        Map<String, String> map = new HashMap<>();
        for (String propertyName : config.getPropertyNames()) {
            String prefix = clientName + "/mp-graphql/" + configKey + "/";
            if (!propertyName.startsWith(prefix)) {
                continue;
            }
            String name = propertyName.substring(prefix.length());
            String value = config.getValue(propertyName, String.class);
            map.put(name, value);
        }
        return map;
    }

}
