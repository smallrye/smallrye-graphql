package io.smallrye.graphql.client.impl.dynamic.cdi;

import java.util.HashMap;
import java.util.Map;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.InjectionPoint;

import io.smallrye.graphql.client.GraphQLClient;
import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClient;
import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClientBuilder;
import io.smallrye.graphql.client.impl.GraphQLClientsConfiguration;

@ApplicationScoped
public class NamedDynamicClients {

    private final String DEFAULT_CLIENT_NAME = "default";

    GraphQLClientsConfiguration globalConfig;

    @PostConstruct
    void initialize() {
        createdClients = new HashMap<>();
        globalConfig = GraphQLClientsConfiguration.getInstance();
    }

    private Map<String, DynamicGraphQLClient> createdClients;

    @Produces
    @Default
    @Dependent
    DynamicGraphQLClient getClient(InjectionPoint ip) {
        GraphQLClient annotation = ip.getAnnotated().getAnnotation(GraphQLClient.class);
        String clientName = annotation != null ? annotation.value() : DEFAULT_CLIENT_NAME;
        return createdClients.computeIfAbsent(clientName,
                name -> DynamicGraphQLClientBuilder.newBuilder().configKey(name).build());
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

}
