package tck.graphql.typesafe;

import static io.smallrye.graphql.client.typesafe.api.AuthorizationHeader.Type.BEARER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.assertj.core.api.BDDAssertions.then;
import static tck.graphql.typesafe.TypesafeGraphQLClientFixture.BASIC_AUTH;
import static tck.graphql.typesafe.TypesafeGraphQLClientFixture.BEARER_AUTH;
import static tck.graphql.typesafe.TypesafeGraphQLClientFixture.withBasicAuth;
import static tck.graphql.typesafe.TypesafeGraphQLClientFixture.withTokenAuth;

import java.lang.annotation.Retention;

import javax.enterprise.inject.Stereotype;

import org.junit.jupiter.api.Test;

import io.smallrye.graphql.client.typesafe.api.AuthorizationHeader;
import io.smallrye.graphql.client.typesafe.api.GraphQLClientApi;

class AuthorizationHeaderBehavior {
    private final TypesafeGraphQLClientFixture fixture = TypesafeGraphQLClientFixture.load();

    @GraphQLClientApi
    interface AuthorizationHeadersApi {
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
        withBasicAuth(AuthorizationHeadersApi.class.getName() + "/mp-graphql/", () -> {
            fixture.returnsData("'apiGreeting':'dummy-greeting'");
            AuthorizationHeadersApi api = fixture.build(AuthorizationHeadersApi.class);

            api.apiGreeting();

            then(fixture.sentHeader("Authorization")).isEqualTo(BASIC_AUTH);
        });
    }

    @Test
    void shouldAddNoPrefixAuthorizationHeader() {
        withBasicAuth("", () -> {
            fixture.returnsData("'plainGreeting':'dummy-greeting'");
            AuthorizationHeadersApi api = fixture.build(AuthorizationHeadersApi.class);

            api.plainGreeting();

            then(fixture.sentHeader("Authorization")).isEqualTo(BASIC_AUTH);
        });
    }

    @Test
    void shouldAddPlainPrefixAuthorizationHeader() {
        withBasicAuth("prefix.", () -> {
            fixture.returnsData("'plainPrefixedGreeting':'dummy-greeting'");
            AuthorizationHeadersApi api = fixture.build(AuthorizationHeadersApi.class);

            api.plainPrefixedGreeting();

            then(fixture.sentHeader("Authorization")).isEqualTo(BASIC_AUTH);
        });
    }

    @Test
    void shouldAddPrefixAuthorizationHeader() {
        withBasicAuth("pre/mp-graphql/", () -> {
            fixture.returnsData("'preGreeting':'dummy-greeting'");
            AuthorizationHeadersApi api = fixture.build(AuthorizationHeadersApi.class);

            api.preGreeting();

            then(fixture.sentHeader("Authorization")).isEqualTo(BASIC_AUTH);
        });
    }

    @GraphQLClientApi
    @AuthorizationHeader(confPrefix = "*")
    interface InheritedAuthorizationHeadersApi extends BaseAuthorizationHeadersApi {
    }

    interface BaseAuthorizationHeadersApi {
        @SuppressWarnings("UnusedReturnValue")
        String greeting();
    }

    @Test
    void shouldAddInheritedAuthorizationHeader() {
        withBasicAuth("", () -> {
            fixture.returnsData("'greeting':'dummy-greeting'");
            InheritedAuthorizationHeadersApi api = fixture.build(InheritedAuthorizationHeadersApi.class);

            api.greeting();

            then(fixture.sentHeader("Authorization")).isEqualTo(BASIC_AUTH);
        });
    }

    @GraphQLClientApi(configKey = "foo")
    @AuthorizationHeader
    interface ConfigKeyAuthorizationHeadersApi {
        @SuppressWarnings("UnusedReturnValue")
        String greeting();
    }

    @Test
    void shouldAddInheritedConfigKeyAuthorizationHeader() {
        withBasicAuth("foo/mp-graphql/", () -> {
            fixture.returnsData("'greeting':'dummy-greeting'");
            ConfigKeyAuthorizationHeadersApi api = fixture.builder()
                    .build(ConfigKeyAuthorizationHeadersApi.class);

            api.greeting();

            then(fixture.sentHeader("Authorization")).isEqualTo(BASIC_AUTH);
        });
    }

    @Retention(RUNTIME)
    @Stereotype
    @AuthorizationHeader(confPrefix = "*")
    @interface Authenticated {
    }

    @GraphQLClientApi
    @Authenticated
    @SuppressWarnings("CdiManagedBeanInconsistencyInspection")
    interface AuthenticatedHeaderApi {
        @SuppressWarnings("UnusedReturnValue")
        String greeting();
    }

    @Test
    void shouldAddStereotypedHeader() {
        withBasicAuth("", () -> {
            fixture.returnsData("'greeting':'dummy-greeting'");
            AuthenticatedHeaderApi api = fixture.build(AuthenticatedHeaderApi.class);

            api.greeting();

            then(fixture.sentHeader("Authorization")).isEqualTo(BASIC_AUTH);
        });
    }

    @GraphQLClientApi
    @AuthorizationHeader(type = BEARER)
    interface TokenAuthorizationHeadersApi {
        @SuppressWarnings("UnusedReturnValue")
        String greeting();

        @SuppressWarnings("UnusedReturnValue")
        @AuthorizationHeader(type = BEARER, confPrefix = "*")
        String plainGreeting();
    }

    @Test
    void shouldAddTokenAuthorizationHeader() {
        withTokenAuth(TokenAuthorizationHeadersApi.class.getName() + "/mp-graphql/", () -> {
            fixture.returnsData("'greeting':'dummy-greeting'");
            TokenAuthorizationHeadersApi api = fixture.build(TokenAuthorizationHeadersApi.class);

            api.greeting();

            then(fixture.sentHeader("Authorization")).isEqualTo(BEARER_AUTH);
        });
    }

    @Test
    void shouldAddPlainTokenAuthorizationHeader() {
        withTokenAuth("", () -> {
            fixture.returnsData("'plainGreeting':'dummy-greeting'");
            TokenAuthorizationHeadersApi api = fixture.build(TokenAuthorizationHeadersApi.class);

            api.plainGreeting();

            then(fixture.sentHeader("Authorization")).isEqualTo(BEARER_AUTH);
        });
    }

}
