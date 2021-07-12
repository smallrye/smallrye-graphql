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
import javax.inject.Inject;

import io.smallrye.graphql.client.ErrorMessageProvider;
import io.smallrye.graphql.client.GraphQLClient;
import io.smallrye.graphql.client.GraphQLClientConfiguration;
import io.smallrye.graphql.client.GraphQLClientsConfiguration;
import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClient;
import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClientBuilder;

@ApplicationScoped
public class NamedDynamicClients {

    private final String DEFAULT_CLIENT_NAME = "default";

    @Inject
    GraphQLClientsConfiguration globalConfig;

    @PostConstruct
    void initialize() {
        createdClients = new HashMap<>();
    }

    private Map<String, DynamicGraphQLClient> createdClients;

    @Produces
    @Default
    @Dependent
    DynamicGraphQLClient getClient(InjectionPoint ip) {
        GraphQLClient annotation = ip.getAnnotated().getAnnotation(GraphQLClient.class);
        String clientName = annotation != null ? annotation.value() : DEFAULT_CLIENT_NAME;
        return createdClients.computeIfAbsent(clientName, name -> {
            GraphQLClientConfiguration config = globalConfig.getClients().get(name);
            if (config == null || config.getUrl() == null) {
                throw ErrorMessageProvider.get().urlMissingErrorForNamedClient(clientName);
            }

            DynamicGraphQLClientBuilder builder = DynamicGraphQLClientBuilder.newBuilder();
            builder = builder.url(config.getUrl());
            builder = builder.configKey(name);
            for (Map.Entry<String, String> headers : config.getHeaders().entrySet()) {
                builder = builder.header(headers.getKey(), headers.getValue());
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

}
