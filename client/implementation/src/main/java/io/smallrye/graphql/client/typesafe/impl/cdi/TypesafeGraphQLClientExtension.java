package io.smallrye.graphql.client.typesafe.impl.cdi;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.WithAnnotations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.smallrye.graphql.client.GraphQLClientsConfiguration;
import io.smallrye.graphql.client.typesafe.api.GraphQLClientApi;

public class TypesafeGraphQLClientExtension implements Extension {
    private static final Logger log = LoggerFactory.getLogger(TypesafeGraphQLClientExtension.class);

    private final List<Class<?>> apis = new ArrayList<>();

    public void registerGraphQLClientApis(@Observes @WithAnnotations(GraphQLClientApi.class) ProcessAnnotatedType<?> type) {
        Class<?> javaClass = type.getAnnotatedType().getJavaClass();
        if (javaClass.isInterface()) {
            log.info("register {}", javaClass.getName());
            apis.add(javaClass);
        } else {
            log.error("failed to register", new IllegalArgumentException(
                    "a GraphQlClientApi must be an interface: " + javaClass.getName()));
        }
    }

    public void createProxies(@Observes AfterBeanDiscovery afterBeanDiscovery) {
        for (Class<?> api : apis) {
            afterBeanDiscovery.addBean(new TypesafeGraphQLClientBean<>(api));
        }
        GraphQLClientsConfiguration.getInstance().addTypesafeClientApis(apis);
    }
}
