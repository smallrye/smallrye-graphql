package io.smallrye.graphql.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    public static void setSingleApplication(boolean singleApplication) {
        GraphQLClientsConfiguration.singleApplication = singleApplication;
    }

    public static GraphQLClientsConfiguration getInstance() {
        ClassLoader key = singleApplication ? null : Thread.currentThread().getContextClassLoader();
        return INSTANCES.computeIfAbsent(key, x -> new GraphQLClientsConfiguration());
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
        List<String> detectedClientNames = new ArrayList<>();
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
            configuration.setUrl(mpConfig.getValue(clientName + "/mp-graphql/url", String.class));
            configuration.setHeaders(getConfigurationValueMap(clientName, "header", mpConfig));
            clients.put(clientName, configuration);
        }
    }

    /**
     * Scan the passed Java interfaces for `@GraphQLClientApi` annotations and create and register
     * client configuration objects for them. This needs to be called by the runtime some time during initialization,
     * before clients are actually created.
     */
    public void addTypesafeClientApis(List<Class<?>> apis) {
        if (apis == null) {
            SmallRyeGraphQLClientLogging.log.apisNotSet();
            apis = Collections.emptyList();
        }
        for (Class<?> api : apis) {
            TypesafeClientConfigurationReader configReader = new TypesafeClientConfigurationReader(api);
            clients.put(configReader.getConfigKey(), configReader.getClientConfiguration());
        }
    }

    public GraphQLClientConfiguration getClient(String key) {
        return clients.get(key);
    }

    public void addClient(String key, GraphQLClientConfiguration config) {
        clients.put(key, config);
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
