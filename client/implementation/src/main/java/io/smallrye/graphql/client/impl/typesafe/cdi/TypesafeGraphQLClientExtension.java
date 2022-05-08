package io.smallrye.graphql.client.impl.typesafe.cdi;

import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;
import jakarta.enterprise.inject.spi.WithAnnotations;

import org.jboss.logging.Logger;

import io.smallrye.graphql.client.impl.GraphQLClientsConfiguration;
import io.smallrye.graphql.client.typesafe.api.GraphQLClientApi;

public class TypesafeGraphQLClientExtension implements Extension {
    private static final Logger log = Logger.getLogger(TypesafeGraphQLClientExtension.class);

    private final List<Class<?>> apis = new ArrayList<>();

    public void registerGraphQLClientApis(@Observes @WithAnnotations(GraphQLClientApi.class) ProcessAnnotatedType<?> type) {
        Class<?> javaClass = type.getAnnotatedType().getJavaClass();
        if (javaClass.isInterface()) {
            log.infof("register %s", javaClass.getName());
            apis.add(javaClass);
        } else {
            log.error("failed to register", new IllegalArgumentException(
                    "a GraphQlClientApi must be an interface: " + javaClass.getName()));
        }
    }

    public void createProxies(@Observes AfterBeanDiscovery afterBeanDiscovery) {
        GraphQLClientsConfiguration clientsConfiguration = GraphQLClientsConfiguration.getInstance();
        for (Class<?> api : apis) {
            afterBeanDiscovery.addBean(new TypesafeGraphQLClientBean<>(api));
            clientsConfiguration.initTypesafeClientApi(api);
        }
    }
}
