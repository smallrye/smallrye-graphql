package io.smallrye.graphql.client.typesafe.api;

import java.net.URI;
import java.util.Iterator;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

public interface GraphQlClientBuilder {

    static GraphQlClientBuilder newBuilder() {
        ServiceLoader<GraphQlClientBuilder> loader = ServiceLoader.load(GraphQlClientBuilder.class);
        Iterator<GraphQlClientBuilder> iterator = loader.iterator();
        if (!iterator.hasNext())
            throw new ServiceConfigurationError("no " + GraphQlClientBuilder.class.getName() + " in classpath");
        GraphQlClientBuilder graphQlClientBuilder = iterator.next();
        if (iterator.hasNext())
            throw new ServiceConfigurationError("more than one " + GraphQlClientBuilder.class.getName() + " in classpath");
        return graphQlClientBuilder;
    }

    GraphQlClientBuilder configKey(String configKey);

    default GraphQlClientBuilder endpoint(String endpoint) {
        return endpoint(URI.create(endpoint));
    }

    GraphQlClientBuilder endpoint(URI endpoint);

    default GraphQlClientBuilder header(String name, Object value) {
        return header(new GraphQlClientHeader(name, value));
    }

    GraphQlClientBuilder header(GraphQlClientHeader header);

    <T> T build(Class<T> apiClass);
}
