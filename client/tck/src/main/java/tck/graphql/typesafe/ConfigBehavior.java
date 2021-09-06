package tck.graphql.typesafe;

import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.assertj.core.api.BDDAssertions.then;

import java.net.URI;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.smallrye.graphql.client.typesafe.api.GraphQLClientApi;

class ConfigBehavior {
    private final TypesafeGraphQLClientFixture fixture = TypesafeGraphQLClientFixture.load();

    @GraphQLClientApi
    interface Api {
        @SuppressWarnings("UnusedReturnValue")
        boolean foo();
    }

    // Can't reuse the Api interface for multiple tests with different configurations, because the property-based configuration for
    // clients is read only once per JVM/process
    @GraphQLClientApi
    interface Api2 {
        @SuppressWarnings("UnusedReturnValue")
        boolean foo();
    }

    @GraphQLClientApi
    interface Api3 {
        @SuppressWarnings("UnusedReturnValue")
        boolean foo();
    }

    @Test
    void shouldFailToLoadMissingEndpointConfig() {
        Throwable thrown = catchThrowableOfType(() -> fixture.builderWithoutEndpointConfig().build(Api.class),
                NoSuchElementException.class);

        then(thrown)
                .hasMessageContaining("SRGQLDC035001")
                .hasMessageContaining(API_URL_CONFIG_KEY);
    }

    @Test
    void shouldLoadEndpointConfig() {
        System.setProperty(API2_URL_CONFIG_KEY, DUMMY_ENDPOINT);
        try {
            fixture.returnsData("'foo':true");
            Api2 api = fixture.builderWithoutEndpointConfig().build(Api2.class);

            api.foo();

            then(fixture.endpointUsed()).isEqualTo(DUMMY_ENDPOINT_URI);
        } finally {
            System.clearProperty(API2_URL_CONFIG_KEY);
        }
    }

    // Disabled - client configuration using config keys is currently read only once at startup, thus this won't be discovered
    // if added so late
    @Disabled
    @Test
    void shouldLoadEndpointFromKeyConfig() {
        System.setProperty("dummy-config-key/mp-graphql/url", DUMMY_ENDPOINT);
        try {
            fixture.returnsData("'foo':true");
            Api3 api = fixture.builderWithoutEndpointConfig()
                    .configKey("dummy-config-key")
                    .build(Api3.class);

            api.foo();

            then(fixture.endpointUsed()).isEqualTo(DUMMY_ENDPOINT_URI);
        } finally {
            System.clearProperty("dummy-config-key/mp-graphql/url");
        }
    }

    @GraphQLClientApi(endpoint = DUMMY_ENDPOINT)
    interface ConfiguredEndpointApi {
        @SuppressWarnings("UnusedReturnValue")
        boolean foo();
    }

    @Test
    void shouldLoadAnnotatedEndpointConfig() {
        fixture.returnsData("'foo':true");
        ConfiguredEndpointApi api = fixture.builderWithoutEndpointConfig().build(ConfiguredEndpointApi.class);

        api.foo();

        then(fixture.endpointUsed()).isEqualTo(DUMMY_ENDPOINT_URI);
    }

    @GraphQLClientApi(configKey = "dummy-config-key")
    interface ConfiguredKeyApi {
        boolean foo();
    }

    @Test
    void shouldLoadAnnotatedKeyConfig() {
        System.setProperty("dummy-config-key/mp-graphql/url", DUMMY_ENDPOINT);
        try {
            fixture.returnsData("'foo':true");
            ConfiguredKeyApi api = fixture.builderWithoutEndpointConfig().build(ConfiguredKeyApi.class);

            api.foo();

            then(fixture.endpointUsed()).isEqualTo(DUMMY_ENDPOINT_URI);
        } finally {
            System.clearProperty("dummy-config-key/mp-graphql/url");
        }
    }

    private static final String API_URL_CONFIG_KEY = Api.class.getName() + "/mp-graphql/url";
    private static final String API2_URL_CONFIG_KEY = Api2.class.getName() + "/mp-graphql/url";
    private static final String DUMMY_ENDPOINT = "http://dummy-configured-endpoint";
    private static final URI DUMMY_ENDPOINT_URI = URI.create(DUMMY_ENDPOINT);
}
