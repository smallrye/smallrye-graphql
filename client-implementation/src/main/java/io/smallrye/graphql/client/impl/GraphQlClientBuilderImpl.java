package io.smallrye.graphql.client.impl;

import java.lang.reflect.Proxy;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.eclipse.microprofile.config.ConfigProvider;

import io.smallrye.graphql.client.api.GraphQlClientApi;
import io.smallrye.graphql.client.api.GraphQlClientBuilder;
import io.smallrye.graphql.client.api.GraphQlClientHeader;
import io.smallrye.graphql.client.impl.reflection.MethodInfo;

public class GraphQlClientBuilderImpl implements GraphQlClientBuilder {
    private String configKey = null;
    private Client client = DEFAULT_CLIENT;
    private URI endpoint;
    private final List<GraphQlClientHeader> headers = new ArrayList<>();

    @Override
    public GraphQlClientBuilder header(GraphQlClientHeader header) {
        headers.add(header);
        return this;
    }

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
        GraphQlClientProxy graphQlClient = new GraphQlClientProxy(webTarget, headers);
        return apiClass.cast(Proxy.newProxyInstance(apiClass.getClassLoader(), new Class<?>[] { apiClass },
                (proxy, method, args) -> graphQlClient.invoke(MethodInfo.of(method, args))));
    }

    private void readConfig(GraphQlClientApi config) {
        if (config == null)
            return;
        if (this.endpoint == null && !config.endpoint().isEmpty())
            this.endpoint = URI.create(config.endpoint());
        if (this.configKey == null && !config.configKey().isEmpty())
            this.configKey = config.configKey();
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
