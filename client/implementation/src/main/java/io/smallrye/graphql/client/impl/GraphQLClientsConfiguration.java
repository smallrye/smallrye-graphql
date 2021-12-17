package io.smallrye.graphql.client.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

/**
 * The wrapper that stores configuration of all GraphQL clients.
 */
public class GraphQLClientsConfiguration {

    private static final Map<ClassLoader, GraphQLClientsConfiguration> INSTANCES = new WeakHashMap<>();
    private static volatile boolean singleApplication = false;

    /**
     * This needs to be set to true if the runtime only supports a single deployment.
     */
    @SuppressWarnings("unused")
    public static void setSingleApplication(boolean singleApplication) {
        GraphQLClientsConfiguration.singleApplication = singleApplication;
    }

    public static GraphQLClientsConfiguration getInstance() {
        ClassLoader key = singleApplication ? null : Thread.currentThread().getContextClassLoader();
        return INSTANCES.computeIfAbsent(key, x -> new GraphQLClientsConfiguration());
    }

    // visible for testing
    public static void clear() {
        INSTANCES.clear();
    }

    /**
     * The map storing the configs of each individual client.
     * <p>
     * The key in this map is:
     * For typesafe clients, the client's `configKey` or, if the `configKey` is not defined, the fully qualified class name
     * For dynamic clients, always the client's `configKey`.
     */
    private final Map<String, GraphQLClientConfiguration> clients = new HashMap<>();

    public GraphQLClientsConfiguration() {
        // Store configuration found in config properties
        Set<String> detectedClientNames = new HashSet<>();
        Config mpConfig = ConfigProvider.getConfig();
        for (String propertyName : mpConfig.getPropertyNames()) {
            // assume that the name of a configured client can consist of
            // uppercase and lowercase letters, numbers, dashes and underscores
            if (propertyName.matches("^[A-Za-z0-9-_.$]+/mp-graphql/.+$")) {
                String key = propertyName.substring(0, propertyName.indexOf("/mp-graphql"));
                if (!clients.containsKey(key)) {
                    detectedClientNames.add(key);
                }
            }
        }
        for (String clientName : detectedClientNames) {
            GraphQLClientConfiguration configuration = new GraphQLClientConfiguration();
            mpConfig.getOptionalValue(clientName + "/mp-graphql/url", String.class)
                    .ifPresent(configuration::setUrl);
            configuration.setHeaders(getConfiguredHeaders(clientName, mpConfig));
            clients.put(clientName, configuration);
        }
    }

    /**
     * Scan the passed Java interface for `@GraphQLClientApi` annotations and create and register
     * client configuration objects for it. This needs to be called by the runtime some time during initialization,
     * before clients are actually created.
     */
    public void initTypesafeClientApi(Class<?> api) {
        TypesafeClientConfigurationReader reader = new TypesafeClientConfigurationReader(api);
        clients.put(reader.getConfigKey(), reader.getClientConfiguration());
    }

    public GraphQLClientConfiguration getClient(String key) {
        return clients.get(key);
    }

    /** All headers that where configured via MP Config, e.g. <code>xxx/mp-graphql/header/yyy = zzz</code> */
    public static Map<String, String> getConfiguredHeaders(String configKey, Config config) {
        Map<String, String> map = new HashMap<>();
        for (String propertyName : config.getPropertyNames()) {
            String prefix = configKey + "/mp-graphql/header/";
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
