package io.smallrye.graphql.client.typesafe.api;

import java.net.URI;
import java.util.Iterator;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

/**
 * Use this builder, when you are not in a CDI context, i.e. when working with Java SE.
 */
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

    /**
     * The base key used to read configuration values. Defaults to the fully qualified name of the API interface.
     */
    GraphQlClientBuilder configKey(String configKey);


    /**
     * The URL where the GraphQL service is listening
     */
    default GraphQlClientBuilder endpoint(String endpoint) {
        return endpoint(URI.create(endpoint));
    }

    /**
     * The URL where the GraphQL service is listening
     */
    GraphQlClientBuilder endpoint(URI endpoint);


    /**
     * A custom http header to send for every GraphQL request.
     */
    default GraphQlClientBuilder header(String name, Object value) {
        return header(new GraphQlClientHeader(name, value));
    }

    /**
     * A custom http header to send for every GraphQL request.
     */
    GraphQlClientBuilder header(GraphQlClientHeader header);


    <T> T build(Class<T> apiClass);
}
