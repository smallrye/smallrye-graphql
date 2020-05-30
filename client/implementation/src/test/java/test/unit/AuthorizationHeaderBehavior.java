package test.unit;

import static io.smallrye.graphql.client.typesafe.api.AuthorizationHeader.Type.BEARER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.assertj.core.api.BDDAssertions.then;

import java.lang.annotation.Retention;

import javax.enterprise.inject.Stereotype;

import org.junit.jupiter.api.Test;

import io.smallrye.graphql.client.typesafe.api.AuthorizationHeader;
import io.smallrye.graphql.client.typesafe.api.GraphQlClientApi;

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
    public void shouldAddApiPrefixAuthorizationHeader() {
        withCredentials(AuthorizationHeadersApi.class.getName() + "/mp-graphql/", () -> {
            fixture.returnsData("'apiGreeting':'dummy-greeting'");
            AuthorizationHeadersApi api = fixture.builder().build(AuthorizationHeadersApi.class);

            api.apiGreeting();

            then(fixture.sentHeader("Authorization")).isEqualTo(BASIC_AUTH);
        });
    }

    @Test
    public void shouldAddNoPrefixAuthorizationHeader() {
        withCredentials("", () -> {
            fixture.returnsData("'plainGreeting':'dummy-greeting'");
            AuthorizationHeadersApi api = fixture.builder().build(AuthorizationHeadersApi.class);

            api.plainGreeting();

            then(fixture.sentHeader("Authorization")).isEqualTo(BASIC_AUTH);
        });
    }

    @Test
    public void shouldAddPlainPrefixAuthorizationHeader() {
        withCredentials("prefix.", () -> {
            fixture.returnsData("'plainPrefixedGreeting':'dummy-greeting'");
            AuthorizationHeadersApi api = fixture.builder().build(AuthorizationHeadersApi.class);

            api.plainPrefixedGreeting();

            then(fixture.sentHeader("Authorization")).isEqualTo(BASIC_AUTH);
        });
    }

    @Test
    public void shouldAddPrefixAuthorizationHeader() {
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
    public void shouldAddInheritedAuthorizationHeader() {
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
    public void shouldAddInheritedConfigKeyAuthorizationHeader() {
        withCredentials("foo/mp-graphql/", () -> {
            fixture.returnsData("'greeting':'dummy-greeting'");
            InheritedConfigKeyAuthorizationHeadersApi api = fixture.builder()
                    .build(InheritedConfigKeyAuthorizationHeadersApi.class);

            api.greeting();

            then(fixture.sentHeader("Authorization")).isEqualTo(BASIC_AUTH);
        });
    }

    @Retention(RUNTIME)
    @Stereotype
    @AuthorizationHeader(confPrefix = "*")
    public @interface Authenticated {
    }

    @GraphQlClientApi
    @Authenticated
    public interface AuthenticatedHeaderApi {
        @SuppressWarnings("UnusedReturnValue")
        String greeting();
    }

    @Test
    public void shouldAddAuthenticatedHeader() {
        withCredentials("", () -> {
            fixture.returnsData("'greeting':'dummy-greeting'");
            AuthenticatedHeaderApi api = fixture.builder().build(AuthenticatedHeaderApi.class);

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
    public void shouldAddTokenAuthorizationHeader() {
        withToken(TokenAuthorizationHeadersApi.class.getName() + "/mp-graphql/", () -> {
            fixture.returnsData("'greeting':'dummy-greeting'");
            TokenAuthorizationHeadersApi api = fixture.builder().build(TokenAuthorizationHeadersApi.class);

            api.greeting();

            then(fixture.sentHeader("Authorization")).isEqualTo(BEARER_AUTH);
        });
    }

    @Test
    public void shouldAddPlainTokenAuthorizationHeader() {
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
