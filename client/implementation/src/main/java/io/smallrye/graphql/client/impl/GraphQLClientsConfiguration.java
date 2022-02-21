package io.smallrye.graphql.client.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    Config mpConfig = ConfigProvider.getConfig();

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
            clients.put(clientName, readConfigurationByKey(clientName));
        }
    }

    private GraphQLClientConfiguration readConfigurationByKey(String clientName) {
        GraphQLClientConfiguration configuration = new GraphQLClientConfiguration();

        // URL
        mpConfig.getOptionalValue(clientName + "/mp-graphql/url", String.class)
                .ifPresent(configuration::setUrl);

        // HTTP headers
        configuration.setHeaders(getConfiguredHeaders(clientName, mpConfig));

        // websocket subprotocols
        Optional<String[]> subprotocolList = mpConfig.getOptionalValue(clientName + "/mp-graphql/subprotocols",
                String[].class);
        configuration.setWebsocketSubprotocols(new ArrayList<>());
        if (subprotocolList.isPresent()) {
            for (String s : subprotocolList.get()) {
                configuration.getWebsocketSubprotocols().add(s);
            }
        }

        // truststore configuration
        Optional<String> truststore = mpConfig.getOptionalValue(clientName + "/mp-graphql/truststore", String.class);
        if (truststore.isPresent()) {
            configuration.setTrustStore(truststore.get());
            configuration.setTrustStorePassword(
                    mpConfig.getOptionalValue(clientName + "/mp-graphql/truststorePassword", String.class).orElse(null));
            configuration.setTrustStoreType(
                    mpConfig.getOptionalValue(clientName + "/mp-graphql/truststoreType", String.class).orElse(null));
        }

        // keystore configuration
        Optional<String> keystore = mpConfig.getOptionalValue(clientName + "/mp-graphql/keystore", String.class);
        if (keystore.isPresent()) {
            configuration.setKeyStore(keystore.get());
            configuration.setKeyStorePassword(
                    mpConfig.getOptionalValue(clientName + "/mp-graphql/keystorePassword", String.class).orElse(null));
            configuration.setKeyStoreType(
                    mpConfig.getOptionalValue(clientName + "/mp-graphql/keystoreType", String.class).orElse(null));
        }

        // proxy settings
        mpConfig.getOptionalValue(clientName + "/mp-graphql/proxyHost", String.class).ifPresent(configuration::setProxyHost);
        mpConfig.getOptionalValue(clientName + "/mp-graphql/proxyPort", Integer.class).ifPresent(configuration::setProxyPort);
        mpConfig.getOptionalValue(clientName + "/mp-graphql/proxyUsername", String.class)
                .ifPresent(configuration::setProxyUsername);
        mpConfig.getOptionalValue(clientName + "/mp-graphql/proxyPassword", String.class)
                .ifPresent(configuration::setProxyPassword);

        mpConfig.getOptionalValue(clientName + "/mp-graphql/maxRedirects", Integer.class)
                .ifPresent(configuration::setMaxRedirects);

        mpConfig.getOptionalValue(clientName + "/mp-graphql/websocketInitializationTimeout", Integer.class)
                .ifPresent(configuration::setWebsocketInitializationTimeout);
        mpConfig.getOptionalValue(clientName + "/mp-graphql/executeSingleOperationsOverWebsocket", Boolean.class)
                .ifPresent(configuration::setExecuteSingleOperationsOverWebsocket);

        return configuration;
    }

    /**
     * Scan the passed Java interfaces for `@GraphQLClientApi` annotations and create and register
     * client configuration objects for them. This needs to be called by the runtime some time during initialization,
     * before clients are actually created.
     *
     * @deprecated use {@link #initTypesafeClientApi(Class)}
     */
    @Deprecated
    public void addTypesafeClientApis(List<Class<?>> apis) {
        if (apis == null) {
            SmallRyeGraphQLClientLogging.log.apisNotSet();
            return;
        }
        apis.forEach(this::initTypesafeClientApi);
    }

    /**
     * Scan the passed Java interface for `@GraphQLClientApi` annotations and create and register
     * client configuration objects for it. This needs to be called by the runtime some time during initialization,
     * before clients are actually created.
     */
    public void initTypesafeClientApi(Class<?> api) {
        TypesafeClientConfigurationReader reader = new TypesafeClientConfigurationReader(api);
        clients.merge(reader.getConfigKey(), reader.getClientConfiguration(), GraphQLClientConfiguration::merge);
    }

    public GraphQLClientConfiguration getClient(String key) {
        return clients.computeIfAbsent(key, this::readConfigurationByKey);
    }

    /** this method is required by Quarkus */
    public void addClient(String key, GraphQLClientConfiguration config) {
        clients.put(key, config);
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
