package io.smallrye.graphql.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

/**
 * The wrapper that stores configuration of all GraphQL clients.
 */
@ApplicationScoped
public class GraphQLClientsConfiguration {

    /**
     * The map storing the configs of each individual client.
     * <p>
     * The key in this map is:
     * For typesafe clients, the client's `configKey` or, if the `configKey` is not defined, the fully qualified class name
     * For dynamic clients, always the client's `configKey`.
     */
    private Map<String, GraphQLClientConfiguration> clients;

    /**
     * This bean needs to know the list of known @GraphQLClientApi classes.
     * This needs to be set before this bean is instantiated.
     * The CDI extension takes care of setting this, but because it can't access the instance
     * of this bean (it does not exist yet), this needs to be a static field.
     * Therefore, to allow multiple applications within one VM without clashing, this static field is a map,
     * where the key is the context class loader of an application.
     */
    private static Map<ClassLoader, List<Class<?>>> apis = new WeakHashMap<>();

    /**
     * Initializes this configuration bean with config from system properties and annotations.
     */
    @PostConstruct
    void initialize() {
        clients = new HashMap<>();

        // store configurations for detected typesafe clients (interfaces with @GraphQLClientApi)
        List<Class<?>> classes = apis.get(Thread.currentThread().getContextClassLoader());
        for (Class<?> api : classes) {
            TypesafeClientConfigurationReader configReader = new TypesafeClientConfigurationReader(api);
            clients.put(configReader.getConfigKey(), configReader.getClientConfiguration());
        }

        // store configured dynamic clients
        // FIXME: move this logic to a class similar to TypesafeClientConfigurationReader?
        List<String> detectedClientNames = new ArrayList<>();
        Config mpConfig = ConfigProvider.getConfig();
        for (String propertyName : mpConfig.getPropertyNames()) {
            // assume that the name of a configured client can consist of
            // uppercase and lowercase letters, numbers, dashes and underscores
            if (propertyName.matches("^[A-Za-z0-9-_]+/mp-graphql/.+$")) {
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

    public Map<String, GraphQLClientConfiguration> getClients() {
        return clients;
    }

    // FIXME: find a better way to let this bean know what api classes are known
    public static void apiClasses(List<Class<?>> apiList) {
        apis.put(Thread.currentThread().getContextClassLoader(), apiList);
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
