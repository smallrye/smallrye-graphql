package test.unit;

import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.Mockito.verify;

import java.net.URI;
import java.util.NoSuchElementException;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;

import org.junit.jupiter.api.Test;

import io.smallrye.graphql.client.typesafe.api.GraphQlClientApi;

class ConfigBehavior {
    private final GraphQlClientFixture fixture = new GraphQlClientFixture();

    @GraphQlClientApi
    interface Api {
        @SuppressWarnings("UnusedReturnValue")
        boolean foo();
    }

    @Test
    void shouldFailToLoadMissingEndpointConfig() {
        Throwable thrown = catchThrowableOfType(() -> fixture.builderWithoutEndpointConfig().build(Api.class),
                NoSuchElementException.class);

        then(thrown)
                .hasMessageContaining("SRCFG00014: Property " + API_URL_CONFIG_KEY)
                .hasMessageContaining("not found");
    }

    @Test
    void shouldLoadEndpointConfig() {
        System.setProperty(API_URL_CONFIG_KEY, DUMMY_ENDPOINT);
        try {
            fixture.returnsData("'foo':true");
            Api api = fixture.builderWithoutEndpointConfig().build(Api.class);

            api.foo();

            then(fixture.endpointUsed()).isEqualTo(DUMMY_ENDPOINT_URI);
        } finally {
            System.clearProperty(API_URL_CONFIG_KEY);
        }
    }

    @Test
    void shouldLoadEndpointFromKeyConfig() {
        System.setProperty("dummy-config-key/mp-graphql/url", DUMMY_ENDPOINT);
        try {
            fixture.returnsData("'foo':true");
            Api api = fixture.builderWithoutEndpointConfig()
                    .configKey("dummy-config-key")
                    .build(Api.class);

            api.foo();

            then(fixture.endpointUsed()).isEqualTo(DUMMY_ENDPOINT_URI);
        } finally {
            System.clearProperty("dummy-config-key/mp-graphql/url");
        }
    }

    @GraphQlClientApi(endpoint = DUMMY_ENDPOINT)
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

    @GraphQlClientApi(configKey = "dummy-config-key")
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

    static class DummyClientRequestFilter implements ClientRequestFilter {
        @Override
        public void filter(ClientRequestContext requestContext) {
        }
    }

    @Test
    void shouldRegisterClientRequestFilterClass() {
        fixture.returnsData("'foo':true");
        Api api = fixture.builder()
                .register(DummyClientRequestFilter.class)
                .build(Api.class);

        api.foo();

        verify(fixture.client()).register(DummyClientRequestFilter.class);
    }

    @Test
    void shouldRegisterClientRequestFilterInstance() {
        fixture.returnsData("'foo':true");
        DummyClientRequestFilter instance = new DummyClientRequestFilter();
        Api api = fixture.builder()
                .register(instance)
                .build(Api.class);

        api.foo();

        verify(fixture.client()).register(instance);
    }

    private static final String API_URL_CONFIG_KEY = Api.class.getName() + "/mp-graphql/url";
    private static final String DUMMY_ENDPOINT = "http://dummy-configured-endpoint";
    private static final URI DUMMY_ENDPOINT_URI = URI.create(DUMMY_ENDPOINT);
}
