package tck.graphql.typesafe;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static tck.graphql.typesafe.CustomAssertions.then;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import jakarta.json.Json;

import org.junit.jupiter.api.Test;

import io.smallrye.graphql.client.GraphQLError;
import io.smallrye.graphql.client.typesafe.api.GraphQLClientApi;
import io.smallrye.graphql.client.typesafe.api.TypesafeResponse;

public class TypesafeResponseBehavior {
    private final TypesafeGraphQLClientFixture fixture = TypesafeGraphQLClientFixture.load();

    @GraphQLClientApi
    interface StringApi {
        TypesafeResponse<String> greetings();

        TypesafeResponse<Greeting> superGreetings();

        TypesafeResponse<TypesafeResponse<Greeting>> anotherGreetings();

        Greeting extraGreetings();
    }

    static class Greeting {
        TypesafeResponse<String> shouldNotWork;
    }

    @Test
    void shouldParseExtensionsInTypesafeResponse() {
        fixture
                .returns("{" +
                        "\"data\":{\"greetings\":\"something\"}," +
                        "\"extensions\":{" +
                        /**/"\"pi\":3.14159," +
                        /**/"\"extension\":\"bell\"" +
                        "}}")
                .withHeaders(
                        Map.of(
                                "Accept", List.of("application/json;charset=utf-8"),
                                "Content-Type", List.of("application/json;charset=utf-8")));

        StringApi api = fixture.build(StringApi.class);

        TypesafeResponse<String> result = api.greetings();
        then(fixture.query()).isEqualTo("query greetings { greetings }");
        then(result.getExtensions()).isEqualTo(Json.createObjectBuilder()
                .add("pi", 3.14159)
                .add("extension", "bell")
                .build());
        then(result.getTransportMeta()).isEqualTo(
                Map.of(
                        "Accept", List.of("application/json;charset=utf-8"),
                        "Content-Type", List.of("application/json;charset=utf-8")));
    }

    @Test
    void shouldHandleErrorsInTypesafeResponse() {
        fixture.returns("{" +
                "\"data\":{\"greetings\":null}," +
                "\"errors\":[{" +
                /**/"\"message\":\"currently can't greet\"," +
                /**/"\"locations\":[{\"line\":1,\"column\":2}]," +
                /**/"\"path\": [\"greetings\"],\n" +
                /**/"\"extensions\":{" +
                /**//**/"\"description\":\"some description\"," +
                /**//**/"\"queryPath\":[\"greeting\"]," +
                /**//**/"\"classification\":\"DataFetchingException\"," +
                /**//**/"\"code\":\"no-greetings\"}" +
                "}]}}");
        StringApi api = fixture.build(StringApi.class);
        assertThrows(NoSuchElementException.class, () -> api.greetings().get());
        GraphQLError error = api.greetings().getErrors().get(0);
        then(error.getMessage()).isEqualTo("currently can't greet");
        then(error.getLocations()).isEqualTo(List.of(Map.of("line", 1, "column", 2)));
        then(error.getPath()).isEqualTo(new Object[] { "greetings" });
        then(error.getDescription()).isEqualTo("some description");
        then(error.getClassification()).isEqualTo("DataFetchingException");
        then(error.getQueryPath()).isEqualTo("[\"greeting\"]");
        then(error.getCode()).isEqualTo("no-greetings");
        then(error.getOtherFields()).isEqualTo(Map.of("__typename", "ErrorOr"));
    }

    @Test
    void shouldThrowExceptionForNestedTypesafeResponse() {
        fixture
                .returns("{" +
                        "\"data\":{\"superGreetings\":{\"shouldNotWork\":\"something\"}}," +
                        "\"extensions\":{" +
                        /**/"\"pi\":3.14159," +
                        /**/"\"extension\":\"bell\"" +
                        "}}")
                .withHeaders(
                        Map.of(
                                "Accept", List.of("application/json;charset=utf-8"),
                                "Content-Type", List.of("application/json;charset=utf-8")));
        StringApi api = fixture.build(StringApi.class);
        assertThrows(IllegalArgumentException.class, () -> api.superGreetings());
    }

    @Test
    void shouldThrowExceptionForGenericTypesafeResponse() {
        fixture
                .returns("{" +
                        "\"data\":{\"anotherGreetings\":\"something\"}," +
                        "\"extensions\":{" +
                        /**/"\"pi\":3.14159," +
                        /**/"\"extension\":\"bell\"" +
                        "}}")
                .withHeaders(
                        Map.of(
                                "Accept", List.of("application/json;charset=utf-8"),
                                "Content-Type", List.of("application/json;charset=utf-8")));
        StringApi api = fixture.build(StringApi.class);
        assertThrows(IllegalArgumentException.class, () -> api.anotherGreetings());
    }

    @Test
    void shouldThrowExceptionForAttributeTypesafeResponse() {
        fixture
                .returns("{" +
                        "\"data\":{\"extraGreetings\":{\"shouldNotWork\":\"something\"}}," +
                        "\"extensions\":{" +
                        /**/"\"pi\":3.14159," +
                        /**/"\"extension\":\"bell\"" +
                        "}}")
                .withHeaders(
                        Map.of(
                                "Accept", List.of("application/json;charset=utf-8"),
                                "Content-Type", List.of("application/json;charset=utf-8")));
        StringApi api = fixture.build(StringApi.class);
        assertThrows(IllegalArgumentException.class, () -> api.extraGreetings());
    }

}
