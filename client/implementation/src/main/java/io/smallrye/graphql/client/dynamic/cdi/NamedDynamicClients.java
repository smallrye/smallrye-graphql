package io.smallrye.graphql.client.dynamic.cdi;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import io.smallrye.graphql.client.GraphQLClient;
import io.smallrye.graphql.client.GraphQLClientsConfiguration;
import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClient;
import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClientBuilder;

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
