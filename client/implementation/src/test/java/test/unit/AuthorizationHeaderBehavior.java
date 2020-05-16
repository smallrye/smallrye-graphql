package test.unit;

import io.smallrye.graphql.client.typesafe.api.AuthorizationHeader;
import io.smallrye.graphql.client.typesafe.api.GraphQlClientApi;
import org.junit.jupiter.api.Test;

import static io.smallrye.graphql.client.typesafe.api.AuthorizationHeader.Type.BEARER;
import static org.assertj.core.api.BDDAssertions.then;

public class AuthorizationHeaderBehavior {
    private final GraphQlClientFixture fixture = new GraphQlClientFixture();

    @GraphQlClientApi
    public interface AuthorizationHeadersApi {
        @SuppressWarnings("UnusedReturnValue")
        @AuthorizationHeader
        String apiGreeting();

        @SuppressWarnings("UnusedReturnValue")
        @AuthorizationHeader(confPrefix = "*")
        String plainGreeting();

        @SuppressWarnings("UnusedReturnValue")
        @AuthorizationHeader(confPrefix = "prefix.*")
        String plainPrefixedGreeting();

        @SuppressWarnings("UnusedReturnValue")
        @AuthorizationHeader(confPrefix = "pre")
        String preGreeting();
    }

    @Test
    void shouldAddApiPrefixAuthorizationHeader() {
        withCredentials(AuthorizationHeadersApi.class.getName() + "/mp-graphql/", () -> {
            fixture.returnsData("'apiGreeting':'dummy-greeting'");
            AuthorizationHeadersApi api = fixture.builder().build(AuthorizationHeadersApi.class);

            api.apiGreeting();

            then(fixture.sentHeader("Authorization")).isEqualTo(BASIC_AUTH);
        });
    }

    @Test
    void shouldAddNoPrefixAuthorizationHeader() {
        withCredentials("", () -> {
            fixture.returnsData("'plainGreeting':'dummy-greeting'");
            AuthorizationHeadersApi api = fixture.builder().build(AuthorizationHeadersApi.class);

            api.plainGreeting();

            then(fixture.sentHeader("Authorization")).isEqualTo(BASIC_AUTH);
        });
    }

    @Test
    void shouldAddPlainPrefixAuthorizationHeader() {
        withCredentials("prefix.", () -> {
            fixture.returnsData("'plainPrefixedGreeting':'dummy-greeting'");
            AuthorizationHeadersApi api = fixture.builder().build(AuthorizationHeadersApi.class);

            api.plainPrefixedGreeting();

            then(fixture.sentHeader("Authorization")).isEqualTo(BASIC_AUTH);
        });
    }

    @Test
    void shouldAddPrefixAuthorizationHeader() {
        withCredentials("pre/mp-graphql/", () -> {
            fixture.returnsData("'preGreeting':'dummy-greeting'");
            AuthorizationHeadersApi api = fixture.builder().build(AuthorizationHeadersApi.class);

            api.preGreeting();

            then(fixture.sentHeader("Authorization")).isEqualTo(BASIC_AUTH);
        });
    }

    @GraphQlClientApi
    @AuthorizationHeader(confPrefix = "*")
    public interface InheritedAuthorizationHeadersApi {
        @SuppressWarnings("UnusedReturnValue")
        String greeting();
    }

    @Test
    void shouldAddInheritedAuthorizationHeader() {
        withCredentials("", () -> {
            fixture.returnsData("'greeting':'dummy-greeting'");
            InheritedAuthorizationHeadersApi api = fixture.builder().build(InheritedAuthorizationHeadersApi.class);

            api.greeting();

            then(fixture.sentHeader("Authorization")).isEqualTo(BASIC_AUTH);
        });
    }

    @GraphQlClientApi(configKey = "foo")
    @AuthorizationHeader
    public interface InheritedConfigKeyAuthorizationHeadersApi {
        @SuppressWarnings("UnusedReturnValue")
        String greeting();
    }

    @Test
    void shouldAddInheritedConfigKeyAuthorizationHeader() {
        withCredentials("foo/mp-graphql/", () -> {
            fixture.returnsData("'greeting':'dummy-greeting'");
            InheritedConfigKeyAuthorizationHeadersApi api = fixture.builder()
                .build(InheritedConfigKeyAuthorizationHeadersApi.class);

            api.greeting();

            then(fixture.sentHeader("Authorization")).isEqualTo(BASIC_AUTH);
        });
    }

    private void withCredentials(String configKey, Runnable runnable) {
        System.setProperty(configKey + "username", "foo");
        System.setProperty(configKey + "password", "bar");
        try {
            runnable.run();
        } finally {
            System.clearProperty(configKey + "username");
            System.clearProperty(configKey + "password");
        }
    }

    @GraphQlClientApi
    @AuthorizationHeader(type = BEARER)
    public interface TokenAuthorizationHeadersApi {
        @SuppressWarnings("UnusedReturnValue")
        String greeting();

        @SuppressWarnings("UnusedReturnValue")
        @AuthorizationHeader(type = BEARER, confPrefix = "*")
        String plainGreeting();
    }

    @Test
    void shouldAddTokenAuthorizationHeader() {
        withToken(TokenAuthorizationHeadersApi.class.getName() + "/mp-graphql/", () -> {
            fixture.returnsData("'greeting':'dummy-greeting'");
            TokenAuthorizationHeadersApi api = fixture.builder().build(TokenAuthorizationHeadersApi.class);

            api.greeting();

            then(fixture.sentHeader("Authorization")).isEqualTo(BEARER_AUTH);
        });
    }

    @Test
    void shouldAddPlainTokenAuthorizationHeader() {
        withToken("", () -> {
            fixture.returnsData("'plainGreeting':'dummy-greeting'");
            TokenAuthorizationHeadersApi api = fixture.builder().build(TokenAuthorizationHeadersApi.class);

            api.plainGreeting();

            then(fixture.sentHeader("Authorization")).isEqualTo(BEARER_AUTH);
        });
    }


    private void withToken(String configKey, Runnable runnable) {
        System.setProperty(configKey + "bearer", "foobar");
        try {
            runnable.run();
        } finally {
            System.clearProperty(configKey + "bearer");
        }
    }

    private static final String BASIC_AUTH = "Basic Zm9vOmJhcg==";
    private static final String BEARER_AUTH = "Bearer foobar";
}
