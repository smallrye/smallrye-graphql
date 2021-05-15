package tck.graphql.typesafe;

import java.net.URI;
import java.util.Iterator;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

import io.smallrye.graphql.client.typesafe.api.TypesafeGraphQLClientBuilder;

/**
 * Builds {@link TypesafeGraphQLClientBuilder} instances with mocked backend and helps testing that.
 */
public interface TypesafeGraphQLClientFixture {
    static TypesafeGraphQLClientFixture load() {
        ServiceLoader<TypesafeGraphQLClientFixture> loader = ServiceLoader.load(TypesafeGraphQLClientFixture.class);
        Iterator<TypesafeGraphQLClientFixture> iterator = loader.iterator();
        if (!iterator.hasNext())
            throw new ServiceConfigurationError("no " + TypesafeGraphQLClientFixture.class.getName() + " in classpath");
        TypesafeGraphQLClientFixture graphQlClientBuilder = iterator.next();
        if (iterator.hasNext())
            throw new ServiceConfigurationError(
                    "more than one " + TypesafeGraphQLClientFixture.class.getName() + " in classpath");
        return graphQlClientBuilder;
    }

    <T> T build(Class<T> apiClass);

    TypesafeGraphQLClientBuilder builder();

    TypesafeGraphQLClientBuilder builderWithoutEndpointConfig();

    void returnsData(String data);

    void returns(String response);

    void returnsServerError();

    String variables();

    String rawVariables();

    String operationName();

    String query();

    Object sentHeader(String name);

    URI endpointUsed();

    void verifyClosed();
}
