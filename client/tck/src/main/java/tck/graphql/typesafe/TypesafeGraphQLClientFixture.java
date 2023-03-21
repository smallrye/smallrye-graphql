package tck.graphql.typesafe;

import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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

    TypesafeGraphQLClientFixture returns(String response);

    void withHeaders(Map<String, List<String>> headers);

    void returnsServerError();

    String variables();

    String rawVariables();

    String operationName();

    String query();

    Map<String, List<String>> transportMeta();

    Object sentHeader(String name);

    URI endpointUsed();

    void verifyClosed();

    String BASIC_AUTH = "Basic Zm9vOmJhcg==";
    String BEARER_AUTH = "Bearer foobar";

    static void withBasicAuth(String configKey, Runnable runnable) {
        withConfig(configKey + "username", "foo", () -> withConfig(configKey + "password", "bar", runnable));
    }

    static void withTokenAuth(String configKey, Runnable runnable) {
        withConfig(configKey + "bearer", "foobar", runnable);
    }

    static void withConfig(String key, String value, Runnable runnable) {
        System.setProperty(key, value);
        try {
            runnable.run();
        } finally {
            System.clearProperty(key);
        }
    }
}
