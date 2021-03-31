package io.smallrye.graphql.client.dynamic.api;

import java.util.Iterator;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

public interface DynamicGraphQLClientBuilder {

    DynamicGraphQLClientBuilder url(String url);

    DynamicGraphQLClient build();

    static DynamicGraphQLClientBuilder newBuilder() {
        ServiceLoader<DynamicGraphQLClientBuilder> loader = ServiceLoader.load(DynamicGraphQLClientBuilder.class);
        Iterator<DynamicGraphQLClientBuilder> iterator = loader.iterator();
        if (!iterator.hasNext())
            throw new ServiceConfigurationError("no " + DynamicGraphQLClientBuilder.class.getName() + " in classpath");
        DynamicGraphQLClientBuilder graphQlClientBuilder = iterator.next();
        if (iterator.hasNext())
            throw new ServiceConfigurationError(
                    "more than one " + DynamicGraphQLClientBuilder.class.getName() + " in classpath");
        return graphQlClientBuilder;
    }
}
