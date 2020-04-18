package test.unit;

import static org.assertj.core.api.BDDAssertions.then;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;

import io.smallrye.graphql.client.typesafe.api.GraphQlClientApi;
import io.smallrye.graphql.client.typesafe.api.GraphQlClientAuthorizationHeader;
import io.smallrye.graphql.client.typesafe.api.GraphQlClientHeader;

class HeaderBehavior {
    private final GraphQlClientFixture fixture = new GraphQlClientFixture();

    interface StringApi {
        @SuppressWarnings("UnusedReturnValue")
        String greeting();
    }

    @Test
    void shouldAddCustomHeader() {
        Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
        fixture.returnsData("'greeting':'dummy-greeting'");
        StringApi api = fixture.builder()
                .header("H1", "V1")
                .header(new GraphQlClientHeader("H2", "V2"))
                .header(new GraphQlClientHeader("H3", clock::instant))
                .build(StringApi.class);

        api.greeting();

        then(fixture.sentHeader("H1")).isEqualTo("V1");
        then(fixture.sentHeader("H2")).isEqualTo("V2");
        then(fixture.sentHeader("H3")).isEqualTo(clock.instant());
    }

    @Test
    void shouldLoadNonAnnotatedKeyConfigAuthHeader() {
        shouldConfigureAuthHeader("", StringApi.class,
                new GraphQlClientAuthorizationHeader());
    }

    @Test
    void shouldLoadNonAnnotatedKeyConfigApiAuthHeader() {
        shouldConfigureAuthHeader(StringApi.class.getName() + "/mp-graphql/",
                StringApi.class,
                new GraphQlClientAuthorizationHeader(StringApi.class));
    }

    @GraphQlClientApi
    interface AnnotatedStringApi extends StringApi {
    }

    @Test
    void shouldLoadAnnotatedKeyConfigApiAuthHeader() {
        shouldConfigureAuthHeader(AnnotatedStringApi.class.getName() + "/mp-graphql/",
                AnnotatedStringApi.class,
                new GraphQlClientAuthorizationHeader(AnnotatedStringApi.class));
    }

    @GraphQlClientApi(configKey = "pre")
    interface ConfiguredKeyStringApi extends StringApi {
    }

    @Test
    void shouldLoadConfiguredKeyConfigApiAuthHeader() {
        shouldConfigureAuthHeader("pre/mp-graphql/", ConfiguredKeyStringApi.class,
                new GraphQlClientAuthorizationHeader(ConfiguredKeyStringApi.class));
    }

    private void shouldConfigureAuthHeader(String expectedConfigKey, Class<? extends StringApi> apiClass,
            GraphQlClientAuthorizationHeader header) {
        System.setProperty(expectedConfigKey + "username", "foo");
        System.setProperty(expectedConfigKey + "password", "bar");
        try {
            fixture.returnsData("'greeting':'dummy-greeting'");
            StringApi api = fixture.builder()
                    .header(header)
                    .build(apiClass);

            api.greeting();

            then(fixture.sentHeader("Authorization")).isEqualTo("Basic Zm9vOmJhcg==");
        } finally {
            System.clearProperty(expectedConfigKey + "username");
            System.clearProperty(expectedConfigKey + "password");
        }
    }
}
