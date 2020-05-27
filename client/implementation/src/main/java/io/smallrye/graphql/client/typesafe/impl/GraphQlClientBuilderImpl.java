package io.smallrye.graphql.client.typesafe.impl;

import java.lang.reflect.Proxy;
import java.net.URI;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.eclipse.microprofile.config.ConfigProvider;

import io.smallrye.graphql.client.typesafe.api.GraphQlClientApi;
import io.smallrye.graphql.client.typesafe.api.GraphQlClientBuilder;
import io.smallrye.graphql.client.typesafe.impl.reflection.MethodInfo;

public class GraphQlClientBuilderImpl implements GraphQlClientBuilder {
    private String configKey = null;
    private Client client = DEFAULT_CLIENT;
    private URI endpoint;

    @Override
    public GraphQlClientBuilder endpoint(URI endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    public GraphQlClientBuilder client(Client client) {
        this.client = client;
        return this;
    }

    @Override
    public GraphQlClientBuilder configKey(String configKey) {
        this.configKey = configKey;
        return this;
    }

    @Override
    public <T> T build(Class<T> apiClass) {
        readConfig(apiClass.getAnnotation(GraphQlClientApi.class));

        WebTarget webTarget = client.target(resolveEndpoint(apiClass));
        GraphQlClientProxy graphQlClient = new GraphQlClientProxy(webTarget);
        return apiClass.cast(Proxy.newProxyInstance(apiClass.getClassLoader(), new Class<?>[] { apiClass },
                (proxy, method, args) -> graphQlClient.invoke(MethodInfo.of(method, args))));
    }

    private void readConfig(GraphQlClientApi annotation) {
        if (annotation == null)
            return;
        if (this.endpoint == null && !annotation.endpoint().isEmpty())
            this.endpoint = URI.create(annotation.endpoint());
        if (this.configKey == null && !annotation.configKey().isEmpty())
            this.configKey = annotation.configKey();
    }

    private URI resolveEndpoint(Class<?> apiClass) {
        if (endpoint != null)
            return endpoint;
        return ConfigProvider.getConfig().getValue(configKey(apiClass) + "/mp-graphql/url", URI.class);
    }

    private String configKey(Class<?> apiClass) {
        return (configKey == null) ? apiClass.getName() : configKey;
    }

    private static final Client DEFAULT_CLIENT = ClientBuilder.newClient();
}
