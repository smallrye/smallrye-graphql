package io.smallrye.graphql.client.typesafe.impl.cdi;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.WithAnnotations;

import io.smallrye.graphql.client.typesafe.api.GraphQlClientApi;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GraphQlClientExtension implements Extension {
    private final List<Class<?>> apis = new ArrayList<>();

    public void registerGraphQlClientApis(@Observes @WithAnnotations(GraphQlClientApi.class) ProcessAnnotatedType<?> type) {
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
            afterBeanDiscovery.addBean(new GraphQlClientBean<>(api));
        }
    }
}
