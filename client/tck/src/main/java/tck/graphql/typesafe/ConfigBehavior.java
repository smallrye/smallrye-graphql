package tck.graphql.typesafe;

import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.assertj.core.api.BDDAssertions.then;
import static tck.graphql.typesafe.TypesafeGraphQLClientFixture.BASIC_AUTH;
import static tck.graphql.typesafe.TypesafeGraphQLClientFixture.withBasicAuth;
import static tck.graphql.typesafe.TypesafeGraphQLClientFixture.withConfig;

import java.net.URI;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.spi.ConfigProviderResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.smallrye.graphql.client.impl.GraphQLClientsConfiguration;
import io.smallrye.graphql.client.typesafe.api.GraphQLClientApi;

class ConfigBehavior {
    private final TypesafeGraphQLClientFixture fixture = TypesafeGraphQLClientFixture.load();

    private final String dummyEndpoint = "http://" + UUID.randomUUID() + ".dummy";
    private final URI dummyEndpointUri = URI.create(dummyEndpoint);

    @BeforeEach
    void setup() {
        GraphQLClientsConfiguration.clear();
        ConfigProviderResolver.instance().releaseConfig(ConfigProvider.getConfig());
    }

    @GraphQLClientApi
    interface Api {
        @SuppressWarnings("UnusedReturnValue")
        boolean foo();
    }

    @Test
    void shouldFailToLoadMissingEndpointConfig() {
        Throwable thrown = catchThrowableOfType(() -> fixture.builderWithoutEndpointConfig().build(Api.class),
                NoSuchElementException.class);

        then(thrown)
                .hasMessageContaining("SRGQLDC035001")
                .hasMessageContaining(Api.class.getName() + "/mp-graphql/url");
    }

    @Test
    void shouldLoadEndpointConfig() {
        withConfig(Api.class.getName() + "/mp-graphql/url", dummyEndpoint, () -> {
            fixture.returnsData("'foo':true");
            Api api = fixture.builderWithoutEndpointConfig().build(Api.class);

            api.foo();

            then(fixture.endpointUsed()).isEqualTo(dummyEndpointUri);
        });
    }

    @Test
    void shouldLoadEndpointFromKeyConfig() {
        withConfig("dummy-config-key/mp-graphql/url", dummyEndpoint, () -> {
            fixture.returnsData("'foo':true");
            Api api = fixture.builderWithoutEndpointConfig()
                    .configKey("dummy-config-key")
                    .build(Api.class);

            api.foo();

            then(fixture.endpointUsed()).isEqualTo(dummyEndpointUri);
        });
    }

    @GraphQLClientApi(endpoint = DUMMY_ENDPOINT)
    interface ConfiguredEndpointApi {
        @SuppressWarnings("UnusedReturnValue")
        boolean foo();
    }

    @Test
    void shouldLoadAnnotatedEndpoint() {
        fixture.returnsData("'foo':true");
        ConfiguredEndpointApi api = fixture.builderWithoutEndpointConfig().build(ConfiguredEndpointApi.class);

        api.foo();

        then(fixture.endpointUsed()).isEqualTo(DUMMY_ENDPOINT_URI);
    }

    @Test
    void shouldLoadAnnotatedEndpointAndCredentialsConfig() {
        withBasicAuth(ConfiguredEndpointApi.class.getName() + "/mp-graphql/", () -> {
            fixture.returnsData("'foo':true");
            ConfiguredEndpointApi api = fixture.builderWithoutEndpointConfig().build(ConfiguredEndpointApi.class);

            api.foo();

            then(fixture.endpointUsed()).isEqualTo(DUMMY_ENDPOINT_URI);
            then(fixture.sentHeader("Authorization")).isEqualTo(BASIC_AUTH);
        });
    }

    @Test
    void shouldLoadAnnotatedEndpointAndHeaderConfig() {
        withConfig(ConfiguredEndpointApi.class.getName() + "/mp-graphql/header/Custom-Header", "some-value", () -> {
            fixture.returnsData("'foo':true");
            ConfiguredEndpointApi api = fixture.builderWithoutEndpointConfig().build(ConfiguredEndpointApi.class);

            api.foo();

            then(fixture.endpointUsed()).isEqualTo(DUMMY_ENDPOINT_URI);
            then(fixture.sentHeader("Custom-Header")).isEqualTo("some-value");
        });
    }

    @Test
    void shouldOverrideDefaultHeadersWithConfig() {
        withConfig(ConfiguredEndpointApi.class.getName() + "/mp-graphql/header/Content-Type", "text/foo",
                () -> withConfig(ConfiguredEndpointApi.class.getName() + "/mp-graphql/header/Accept", "text/bar", () -> {
                    fixture.returnsData("'foo':true");
                    ConfiguredEndpointApi api = fixture.builderWithoutEndpointConfig().build(ConfiguredEndpointApi.class);

                    api.foo();

                    then(fixture.sentHeader("Content-Type")).hasToString("text/foo");
                    then(fixture.sentHeader("Accept")).hasToString("text/bar");
                }));
    }

    @GraphQLClientApi(configKey = "dummy-config-key")
    interface ConfiguredKeyApi {
        boolean foo();
    }

    @Test
    void shouldLoadWithAnnotatedConfigKey() {
        withConfig("dummy-config-key/mp-graphql/url", dummyEndpoint, () -> {
            fixture.returnsData("'foo':true");
            ConfiguredKeyApi api = fixture.builderWithoutEndpointConfig().build(ConfiguredKeyApi.class);

            api.foo();

            then(fixture.endpointUsed()).isEqualTo(dummyEndpointUri);
        });
    }

    private static final String DUMMY_ENDPOINT = "http://dummy-configured-endpoint";
    private static final URI DUMMY_ENDPOINT_URI = URI.create(DUMMY_ENDPOINT);
}
