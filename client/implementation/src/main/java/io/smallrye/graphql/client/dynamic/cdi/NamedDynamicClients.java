package io.smallrye.graphql.client.dynamic.cdi;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

import io.smallrye.graphql.client.NamedClient;
import io.smallrye.graphql.client.SmallRyeGraphQLClientMessages;
import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClient;
import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClientBuilder;

@ApplicationScoped
public class NamedDynamicClients {

    private static final String CONFIG_KEY_URL = "url";
    private static final String CONFIG_KEY_HEADER = "header";
    private Config config;

    private final String DEFAULT_CLIENT_NAME = "default";

    @PostConstruct
    void initialize() {
        createdClients = new HashMap<>();
        config = ConfigProvider.getConfig();
    }

    private Map<String, DynamicGraphQLClient> createdClients;

    @Produces
    @Default
    @Dependent
    DynamicGraphQLClient getClient(InjectionPoint ip) {
        NamedClient annotation = ip.getAnnotated().getAnnotation(NamedClient.class);
        String clientName = annotation != null ? annotation.value() : DEFAULT_CLIENT_NAME;
        return createdClients.computeIfAbsent(clientName, name -> {
            DynamicGraphQLClientBuilder builder = DynamicGraphQLClientBuilder.newBuilder();
            try {
                builder = builder.url(getConfigurationValue(clientName, CONFIG_KEY_URL));
            } catch (NoSuchElementException e) {
                throw SmallRyeGraphQLClientMessages.msg.urlNotConfiguredForNamedClient(clientName);
            }
            // determine the HTTP headers
            for (Map.Entry<String, String> header : getConfigurationValueMap(clientName, CONFIG_KEY_HEADER).entrySet()) {
                builder = builder.header(header.getKey(), header.getValue());
            }
            return builder.build();
        });
    }

    @PreDestroy
    void cleanup() {
        createdClients.values().forEach(client -> {
            try {
                client.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private String getConfigurationValue(String clientName, String configKey) {
        return config.getValue("smallrye.graphql.client." + clientName + "/" + configKey, String.class);
    }

    /**
     * For example, if there's a named client 'client1' with this:
     * smallrye.graphql.client.client1/header/Foo=Bar
     * smallrye.graphql.client.client1/header/A=B
     *
     * then getConfigurationValueMap(client1, header) will return a map containing Foo=Bar and A=B
     */
    private Map<String, String> getConfigurationValueMap(String clientName, String configKey) {
        Map<String, String> map = new HashMap<>();
        for (String propertyName : config.getPropertyNames()) {
            String prefix = "smallrye.graphql.client." + clientName + "/" + configKey + "/";
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
