package io.smallrye.graphql.client.typesafe.jaxrs;

import java.lang.reflect.Proxy;
import java.net.URI;
import java.security.AccessController;
import java.security.PrivilegedAction;

import javax.enterprise.inject.spi.CDI;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import io.smallrye.graphql.client.GraphQLClientConfiguration;
import io.smallrye.graphql.client.GraphQLClientsConfiguration;
import io.smallrye.graphql.client.typesafe.api.GraphQLClientApi;
import io.smallrye.graphql.client.typesafe.api.TypesafeGraphQLClientBuilder;
import io.smallrye.graphql.client.typesafe.impl.reflection.MethodInvocation;

public class JaxRsTypesafeGraphQLClientBuilder implements TypesafeGraphQLClientBuilder {
    private String configKey = null;
    private Client client;
    private URI endpoint;

    @Override
    public TypesafeGraphQLClientBuilder configKey(String configKey) {
        this.configKey = configKey;
        return this;
    }

    public TypesafeGraphQLClientBuilder client(Client client) {
        this.client = client;
        return this;
    }

    private Client client() {
        if (client == null)
            client = ClientBuilder.newClient();
        return client;
    }

    @Override
    public TypesafeGraphQLClientBuilder endpoint(URI endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    public TypesafeGraphQLClientBuilder register(Class<?> componentClass) {
        client().register(componentClass);
        return this;
    }

    public TypesafeGraphQLClientBuilder register(Object component) {
        client().register(component);
        return this;
    }

    @Override
    public <T> T build(Class<T> apiClass) {
        if (configKey == null) {
            configKey = configKey(apiClass);
        }

        GraphQLClientConfiguration persistentConfig;
        try {
            persistentConfig = CDI.current()
                    .select(GraphQLClientsConfiguration.class).get()
                    .getClients().get(configKey);
        } catch (IllegalStateException ex) {
            // Probably running in a non-CDI environment so we can't use the GraphQLClientsConfiguration bean
            // TODO: right now, the GraphQLClientsConfiguration contains only metadata derived
            // from config properties. Once we move annotation-derived metadata there, that bean will
            // always be necessary (otherwise the client would ignore annotations within GraphQLClientApi interfaces)
            // so we need to make sure that the config bean will be available without CDI
            // - perhaps fall back to a pure Java singleton if CDI is not available?
            persistentConfig = null;
        }
        if (persistentConfig != null) {
            applyConfig(persistentConfig);
        }

        WebTarget webTarget = client().target(endpoint);
        JaxRsTypesafeGraphQLClientProxy graphQlClient = new JaxRsTypesafeGraphQLClientProxy(webTarget, persistentConfig);
        return apiClass.cast(Proxy.newProxyInstance(getClassLoader(apiClass), new Class<?>[] { apiClass },
                (proxy, method, args) -> invoke(apiClass, graphQlClient, method, args)));
    }

    private Object invoke(Class<?> apiClass, JaxRsTypesafeGraphQLClientProxy graphQlClient, java.lang.reflect.Method method,
            Object... args) {
        MethodInvocation methodInvocation = MethodInvocation.of(method, args);
        if (methodInvocation.isDeclaredInCloseable()) {
            client().close();
            return null; // void
        }
        return graphQlClient.invoke(apiClass, methodInvocation);
    }

    private ClassLoader getClassLoader(Class<?> apiClass) {
        if (System.getSecurityManager() == null)
            return apiClass.getClassLoader();
        return AccessController.doPrivileged((PrivilegedAction<ClassLoader>) apiClass::getClassLoader);
    }

    /**
     * Applies values from known global configuration. This does NOT override values passed to this
     * builder by method calls.
     */
    private void applyConfig(GraphQLClientConfiguration configuration) {
        if (this.endpoint == null && configuration.getUrl() != null) {
            this.endpoint = URI.create(configuration.getUrl());
        }
    }

    private String configKey(Class<?> apiClass) {
        GraphQLClientApi annotation = apiClass.getAnnotation(GraphQLClientApi.class);
        if (annotation == null) {
            return apiClass.getName();
        }
        String keyFromAnnotation = annotation.configKey();
        return (keyFromAnnotation.isEmpty()) ? apiClass.getName() : keyFromAnnotation;
    }
}
