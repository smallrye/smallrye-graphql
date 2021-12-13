package tck.graphql.typesafe;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static tck.graphql.typesafe.CustomAssertions.then;

import java.util.List;
import java.util.NoSuchElementException;

import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.NonNull;
import org.junit.jupiter.api.Test;

import io.smallrye.graphql.client.GraphQLClientException;
import io.smallrye.graphql.client.GraphQLError;
import io.smallrye.graphql.client.InvalidResponseException;
import io.smallrye.graphql.client.typesafe.api.ErrorOr;
import io.smallrye.graphql.client.typesafe.api.GraphQLClientApi;

class ErrorBehavior {
    private final TypesafeGraphQLClientFixture fixture = TypesafeGraphQLClientFixture.load();

    @GraphQLClientApi
    interface StringApi {
        @SuppressWarnings("UnusedReturnValue")
        String greeting();
    }

    @Test
    void shouldFailOnQueryError() {
        fixture.returns("{" +
                "\"data\":{\"greeting\":null}," +
                "\"errors\":[{" +
                /**/"\"message\":\"currently can't greet\"," +
                /**/"\"locations\":[{\"line\":1,\"column\":2}]," +
                /**/"\"path\": [\"greeting\"],\n" +
                /**/"\"extensions\":{" +
                /**//**/"\"description\":\"some description\"," +
                /**//**/"\"queryPath\":[\"greeting\"]," +
                /**//**/"\"classification\":\"DataFetchingException\"," +
                /**//**/"\"code\":\"no-greeting\"}" +
                "}]}}");
        StringApi api = fixture.build(StringApi.class);

        GraphQLClientException thrown = catchThrowableOfType(api::greeting, GraphQLClientException.class);

        then(thrown).hasExactlyOneErrorWhich()
                .hasMessage("currently can't greet")
                .hasSourceLocation(1, 2)
                .hasPath("greeting")
                .hasErrorCode("no-greeting");
    }

    @Test
    void shouldFailOnValidationError() {
        fixture.returns("{\n" +
                "  \"errors\": [\n" +
                "    {\n" +
                "      \"message\": \"Validation error of type FieldUndefined: Field 'foo' in type 'Query' is undefined @ 'foo'\",\n"
                +
                "      \"locations\": [\n" +
                "        {\n" +
                "          \"line\": 1,\n" +
                "          \"column\": 8\n" +
                "        }\n" +
                "      ]," +
                "      \"extensions\": {\n" +
                "        \"description\": \"Field 'foo' in type 'Query' is undefined\",\n" +
                "        \"validationErrorType\": \"FieldUndefined\",\n" +
                "        \"queryPath\": [\n" +
                "          \"foo\"\n" +
                "        ],\n" +
                "        \"classification\": \"ValidationError\"\n" +
                "      }\n" +
                "    }\n" +
                "  ],\n" +
                "  \"data\": null\n" +
                "}\n");
        StringApi api = fixture.build(StringApi.class);

        GraphQLClientException thrown = catchThrowableOfType(api::greeting, GraphQLClientException.class);

        then(thrown).hasExactlyOneErrorWhich()
                .hasMessage("Validation error of type FieldUndefined: Field 'foo' in type 'Query' is undefined @ 'foo'")
                .hasSourceLocation(1, 8)
                .hasExtension("description", "Field 'foo' in type 'Query' is undefined")
                .hasExtension("validationErrorType", "FieldUndefined")
                .hasExtension("classification", "ValidationError")
                .hasErrorCode(null);
    }

    @Test
    void shouldFailOnErrorWithoutMessage() {
        fixture.returns("{" +
                "\"data\":{\"greeting\":null}," +
                "\"errors\":[{" +
                /**/"\"locations\":[{\"line\":1,\"column\":2,\"sourceName\":\"loc\"}]," +
                /**/"\"path\": [\"greeting\"],\n" +
                /**/"\"extensions\":{" +
                /**//**/"\"description\":\"some description\"," +
                /**//**/"\"queryPath\":[\"greeting\"]," +
                /**//**/"\"classification\":\"DataFetchingException\"," +
                /**//**/"\"code\":\"no-greeting\"}" +
                "}]}}");
        StringApi api = fixture.build(StringApi.class);

        GraphQLClientException thrown = catchThrowableOfType(api::greeting, GraphQLClientException.class);

        then(thrown).hasExactlyOneErrorWhich()
                .hasMessage(null)
                .hasSourceLocation(1, 2)
                .hasPath("greeting")
                .hasErrorCode("no-greeting");
    }

    @Test
    void shouldFailOnErrorWithNullMessage() {
        fixture.returns("{\n" +
                "  \"errors\": [\n" +
                "    {\n" +
                "      \"message\": null,\n" +
                "      \"extensions\": {\n" +
                "        \"classification\": \"SomeClassification\"\n" +
                "      }\n" +
                "    }\n" +
                "  ],\n" +
                "  \"data\": null\n" +
                "}\n");
        StringApi api = fixture.build(StringApi.class);

        GraphQLClientException thrown = catchThrowableOfType(api::greeting, GraphQLClientException.class);

        then(thrown).hasExactlyOneErrorWhich()
                .hasMessage(null)
                .hasNoSourceLocation()
                .hasClassification("SomeClassification")
                .hasErrorCode(null);
    }

    @Test
    void shouldFailOnErrorWithoutExtensions() {
        fixture.returns("{\n" +
                "  \"errors\": [\n" +
                "    {\n" +
                "      \"message\": \"something went wrong\",\n" +
                "      \"locations\": [\n" +
                "        {\n" +
                "          \"line\": 1,\n" +
                "          \"column\": 8\n" +
                "        }\n" +
                "      ]" +
                "    }\n" +
                "  ],\n" +
                "  \"data\": null\n" +
                "}\n");
        StringApi api = fixture.build(StringApi.class);

        GraphQLClientException thrown = catchThrowableOfType(api::greeting, GraphQLClientException.class);

        then(thrown).hasExactlyOneErrorWhich()
                .hasMessage("something went wrong")
                .hasSourceLocation(1, 8)
                .hasErrorCode(null);
    }

    @Test
    void shouldFailOnErrorWithoutLocations() {
        fixture.returns("{\n" +
                "  \"errors\": [\n" +
                "    {\n" +
                "      \"message\": \"something went wrong\",\n" +
                "      \"extensions\": {\n" +
                "        \"classification\": \"SomeClassification\"\n" +
                "      }\n" +
                "    }\n" +
                "  ],\n" +
                "  \"data\": null\n" +
                "}\n");
        StringApi api = fixture.build(StringApi.class);

        GraphQLClientException thrown = catchThrowableOfType(api::greeting, GraphQLClientException.class);

        then(thrown).hasExactlyOneErrorWhich()
                .hasMessage("something went wrong")
                .hasNoSourceLocation()
                .hasClassification("SomeClassification")
                .hasErrorCode(null);
    }

    @Test
    void shouldFailOnErrorWithEmptyLocations() {
        fixture.returns("{\n" +
                "  \"errors\": [\n" +
                "    {\n" +
                "      \"message\": \"something went wrong\",\n" +
                "      \"locations\": []," +
                "      \"extensions\": {\n" +
                "        \"classification\": \"SomeClassification\"\n" +
                "      }\n" +
                "    }\n" +
                "  ],\n" +
                "  \"data\": null\n" +
                "}\n");
        StringApi api = fixture.build(StringApi.class);

        GraphQLClientException thrown = catchThrowableOfType(api::greeting, GraphQLClientException.class);

        then(thrown).hasExactlyOneErrorWhich()
                .hasMessage("something went wrong")
                .hasSourceLocations()
                .hasClassification("SomeClassification")
                .hasErrorCode(null);
    }

    @Test
    void shouldFailStringQueryNotFound() {
        fixture.returnsServerError();
        StringApi api = fixture.build(StringApi.class);

        RuntimeException thrown = catchThrowableOfType(api::greeting, RuntimeException.class);

        then(thrown).hasMessage("expected successful status code but got 500 Internal Server Error:\nfailed");
    }

    @Test
    void shouldFailOnMissingQueryResponse() {
        fixture.returnsData("");
        StringApi api = fixture.build(StringApi.class);

        InvalidResponseException thrown = catchThrowableOfType(api::greeting, InvalidResponseException.class);

        then(thrown).hasMessageContaining("No data for 'greeting'");
    }

    @Test
    void shouldIgnoreEmptyError() {
        fixture.returns("{\"errors\":[], \"data\":{\"greeting\":\"dummy-greeting\"}}");
        StringApi api = fixture.build(StringApi.class);

        String greeting = api.greeting();

        then(fixture.query()).isEqualTo("query greeting { greeting }");
        then(greeting).isEqualTo("dummy-greeting");
    }

    static class SuperHero {
        String name;
        ErrorOr<String> location;
    }

    static class Team {
        @SuppressWarnings("unused")
        String name;
    }

    @GraphQLClientApi
    interface SuperHeroApi {
        ErrorOr<List<Team>> teams();
    }

    @Test
    void shouldFetchErrorOrPresent() {
        fixture.returns("{\"data\":{\"teams\":[{\"name\":\"Avengers\"}]}}");
        SuperHeroApi api = fixture.build(SuperHeroApi.class);

        ErrorOr<List<Team>> response = api.teams();

        then(fixture.query()).isEqualTo("query teams { teams {name} }");
        then(response.isPresent()).isTrue();
        then(response.hasErrors()).isFalse();
        then(catchThrowable(response::getErrors)).isInstanceOf(NoSuchElementException.class);
        then(response.get()).hasSize(1);
        then(response.get().get(0).name).isEqualTo("Avengers");
    }

    @Test
    void shouldFetchErrorOrAbsent() {
        fixture.returns("{" +
                "\"data\":{\"teams\":null}," +
                "\"errors\":[{" +
                /**/"\"message\":\"currently can't search for teams\"," +
                /**/"\"locations\":[{\"line\":1,\"column\":2,\"sourceName\":\"loc\"}]," +
                /**/"\"path\": [\"teams\"],\n" +
                /**/"\"extensions\":{" +
                /**//**/"\"description\":\"Field 'foo' in type 'Query' is undefined\"," +
                /**//**/"\"validationErrorType\":\"FieldUndefined\"," +
                /**//**/"\"queryPath\":[\"foo\"]," +
                /**//**/"\"classification\":\"ValidationError\"," +
                /**//**/"\"code\":\"team-search-disabled\"}" +
                "}]}}");
        SuperHeroApi api = fixture.build(SuperHeroApi.class);

        ErrorOr<List<Team>> response = api.teams();

        then(fixture.query()).isEqualTo("query teams { teams {name} }");
        then(response).hasExactlyOneErrorWhich()
                .hasMessage("currently can't search for teams")
                .hasPath("teams")
                .hasSourceLocation(1, 2)
                .hasErrorCode("team-search-disabled");
    }

    @Test
    void shouldFetchErrorOnNullData() {
        fixture.returns("{" +
                "\"data\":null," +
                "\"errors\":[{" +
                /**/"\"message\":\"currently can't search for teams\"," +
                /**/"\"locations\":[{\"line\":1,\"column\":2,\"sourceName\":\"loc\"}]," +
                /**/"\"path\": [\"teams\"],\n" +
                /**/"\"extensions\":{" +
                /**//**/"\"description\":\"Field 'foo' in type 'Query' is undefined\"," +
                /**//**/"\"validationErrorType\":\"FieldUndefined\"," +
                /**//**/"\"queryPath\":[\"foo\"]," +
                /**//**/"\"classification\":\"ValidationError\"," +
                /**//**/"\"code\":\"team-search-disabled\"}" +
                "}]}}");
        SuperHeroApi api = fixture.build(SuperHeroApi.class);

        GraphQLClientException throwable = catchThrowableOfType(api::teams, GraphQLClientException.class);

        then(fixture.query()).isEqualTo("query teams { teams {name} }");
        then(throwable).hasExactlyOneErrorWhich()
                .hasMessage("currently can't search for teams")
                .hasPath("teams")
                .hasSourceLocation(1, 2)
                .hasErrorCode("team-search-disabled");
    }

    @Test
    void shouldFetchErrorOrWithTwoErrors() {
        fixture.returns("{" +
                "\"data\":{\"teams\":null}," +
                "\"errors\":[{" +
                /**/"\"message\":\"currently can't search for teams\"," +
                /**/"\"locations\":[{\"line\":1,\"column\":2,\"sourceName\":\"loc\"}]," +
                /**/"\"path\": [\"teams\"],\n" +
                /**/"\"extensions\":{" +
                /**//**/"\"description\":\"Field 'foo' in type 'Query' is undefined\"," +
                /**//**/"\"validationErrorType\":\"FieldUndefined\"," +
                /**//**/"\"queryPath\":[\"foo\"]," +
                /**//**/"\"classification\":\"ValidationError\"," +
                /**//**/"\"code\":\"team-search-disabled\"}" +
                /**/"},{" +
                /**/"\"message\":\"feeling dizzy\"," +
                /**/"\"locations\":[{\"line\":2,\"column\":3,\"sourceName\":\"lock\"}]," +
                /**/"\"path\": [\"teams\"],\n" +
                /**/"\"extensions\":{" +
                /**//**/"\"description\":\"not feeling so well\"," +
                /**//**/"\"queryPath\":[\"bar\"]," +
                /**//**/"\"code\":\"dizzy\"}" +
                "}]}}");
        SuperHeroApi api = fixture.build(SuperHeroApi.class);

        ErrorOr<List<Team>> response = api.teams();

        then(fixture.query()).isEqualTo("query teams { teams {name} }");
        List<GraphQLError> errors = response.getErrors();
        then(errors).hasSize(2);
        then(errors.get(0))
                .hasMessage("currently can't search for teams")
                .hasPath("teams")
                .hasSourceLocation(1, 2)
                .hasErrorCode("team-search-disabled");

        then(errors.get(1))
                .hasMessage("feeling dizzy")
                .hasPath("teams")
                .hasSourceLocation(2, 3)
                .hasErrorCode("dizzy");
    }

    @Test
    void shouldFetchErrorOrAbsentWithoutPath() {
        fixture.returns("{" +
                "\"data\":{\"teams\":null}," +
                "\"errors\":[{" +
                /**/"\"message\":\"currently can't search for teams\"," +
                /**/"\"locations\":[{\"line\":1,\"column\":2,\"sourceName\":\"loc\"}]," +
                /**/"\"extensions\":{" +
                /**//**/"\"description\":\"Field 'foo' in type 'Query' is undefined\"," +
                /**//**/"\"validationErrorType\":\"FieldUndefined\"," +
                /**//**/"\"queryPath\":[\"foo\"]," +
                /**//**/"\"classification\":\"ValidationError\"," +
                /**//**/"\"code\":\"team-search-disabled\"}" +
                "}]}}");
        SuperHeroApi api = fixture.build(SuperHeroApi.class);

        GraphQLClientException throwable = catchThrowableOfType(api::teams, GraphQLClientException.class);

        then(fixture.query()).isEqualTo("query teams { teams {name} }");
        then(throwable).hasExactlyOneErrorWhich()
                .hasMessage("currently can't search for teams")
                .hasNoPath()
                .hasSourceLocation(1, 2)
                .hasErrorCode("team-search-disabled");
    }

    @Test
    void shouldFetchErrorOrWithNullInPath() {
        fixture.returns("{" +
                "\"data\":{\"teams\":null}," +
                "\"errors\":[{" +
                /**/"\"message\":\"can't get team name\"," +
                /**/"\"locations\":[{\"line\":1,\"column\":2,\"sourceName\":\"loc\"}]," +
                /**/"\"path\": [\"teams\",\"name\"],\n" +
                /**/"\"extensions\":{" +
                /**//**/"\"code\":\"team-name-disabled\"}" +
                "}]}}");
        SuperHeroApi api = fixture.build(SuperHeroApi.class);

        GraphQLClientException throwable = catchThrowableOfType(api::teams, GraphQLClientException.class);

        then(fixture.query()).isEqualTo("query teams { teams {name} }");
        then(throwable).hasExactlyOneErrorWhich()
                .hasMessage("can't get team name")
                .hasPath("teams", "name")
                .hasSourceLocation(1, 2)
                .hasErrorCode("team-name-disabled");
    }

    static class Wrapper {
        @Name("findHeroes")
        List<ErrorOr<SuperHero>> superHeroes;
        @Name("findTeams")
        ErrorOr<List<Team>> teams;
    }

    @GraphQLClientApi
    interface SuperHeroWrappedApi {
        Wrapper find();
    }

    @Test
    void shouldFetchFullWrapper() {
        fixture.returns("{\"data\":{\"find\":{" +
                "\"superHeroes\":[{\"name\":\"Spider-Man\",\"location\":\"New York\"}]," +
                "\"teams\":[{\"name\":\"Avengers\"}]" +
                "}}}");
        SuperHeroWrappedApi api = fixture.build(SuperHeroWrappedApi.class);

        Wrapper response = api.find();

        then(fixture.query()).isEqualTo("query find { find {superHeroes:findHeroes {name location} teams:findTeams {name}} }");
        {
            then(response.superHeroes).hasSize(1);
            ErrorOr<SuperHero> superHero = response.superHeroes.get(0);
            then(superHero.isPresent()).isTrue();
            then(superHero.hasErrors()).isFalse();
            then(superHero.get().name).isEqualTo("Spider-Man");
            then(superHero.get().location.isPresent()).isTrue();
            then(superHero.get().location.hasErrors()).isFalse();
            then(superHero.get().location.get()).isEqualTo("New York");
        }
        {
            then(response.teams.isPresent()).isTrue();
            then(response.teams.hasErrors()).isFalse();
            then(catchThrowable(() -> response.teams.getErrors())).isInstanceOf(NoSuchElementException.class);
            List<Team> teams = response.teams.get();
            then(teams).hasSize(1);
            then(teams.get(0).name).isEqualTo("Avengers");
        }
    }

    @Test
    void shouldFetchPartialWrapper() {
        fixture.returns("{" +
                "\"data\":{\"find\":{\"superHeroes\":[{\"name\":\"Wolverine\"}],\"teams\":null}}," +
                "\"errors\":[{" +
                /**/"\"message\":\"currently can't search for teams\"," +
                /**/"\"locations\":[{\"line\":1,\"column\":2,\"sourceName\":\"loc\"}]," +
                /**/"\"path\": [\"find\",\"teams\"],\n" +
                /**/"\"extensions\":{\"code\":\"team-search-disabled\"}" +
                "}]}}");
        SuperHeroWrappedApi api = fixture.build(SuperHeroWrappedApi.class);

        Wrapper response = api.find();

        then(fixture.query()).isEqualTo("query find { find {superHeroes:findHeroes {name location} teams:findTeams {name}} }");
        then(response.superHeroes).hasSize(1);
        then(response.superHeroes.get(0).get().name).isEqualTo("Wolverine");
        then(response.teams.hasErrors()).isTrue();
        then(response.teams.isPresent()).isFalse();
        then(catchThrowable(() -> response.teams.get())).isInstanceOf(NoSuchElementException.class);
        then(response.teams).hasExactlyOneErrorWhich()
                .hasMessage("currently can't search for teams")
                .hasPath("find", "teams")
                .hasSourceLocation(1, 2)
                .hasErrorCode("team-search-disabled");
    }

    @GraphQLClientApi
    interface OrderApi {
        @SuppressWarnings("UnusedReturnValue")
        Order order(@NonNull String id);
    }

    @SuppressWarnings("unused")
    public static class Order {
        public String id;
        public String orderDate;
        public List<OrderItem> items;
    }

    public static class OrderItem {
        @SuppressWarnings("unused")
        public Product product;
    }

    public static class Product {
        @SuppressWarnings("unused")
        public String id;
        public String name;
    }

    @Test
    void shouldFetchComplexError() {
        fixture.returns("{\"errors\":[" +
                "{" +
                "\"message\":\"System error\"," +
                "\"locations\":[{\"line\":1,\"column\":84}]," +
                "\"path\":[\"order\",\"items\",0,\"product\"]" +
                "}" +
                "]," +
                "\"data\":{" +
                "\"order\":{" +
                "\"id\":\"o1\"," +
                "\"items\":[" +
                "{\"product\":null}," +
                "{\"product\":null}" +
                "]}}}");
        OrderApi api = fixture.build(OrderApi.class);

        GraphQLClientException throwable = catchThrowableOfType(() -> api.order("o1"), GraphQLClientException.class);

        then(fixture.query())
                .isEqualTo("query order($id: String!) { order(id: $id) {id orderDate items {product {id name}}} }");
        then(fixture.variables()).isEqualTo("{'id':'o1'}");
        then(throwable).hasExactlyOneErrorWhich()
                .hasMessage("System error")
                .hasPath("order", "items", 0, "product")
                .hasSourceLocation(1, 84)
                .hasErrorCode(null);
    }

    @GraphQLClientApi
    interface DeepErrorApi {
        @SuppressWarnings("UnusedReturnValue")
        OuterContainer outer();
    }

    static class OuterContainer {
        @SuppressWarnings("unused")
        InnerContainer inner;
    }

    static class InnerContainer {
        @SuppressWarnings("unused")
        String content;
    }

    @Test
    void shouldFailToMapDirectNestedError() {
        fixture.returns(deeplyNestedError("null"));
        DeepErrorApi api = fixture.build(DeepErrorApi.class);

        GraphQLClientException throwable = catchThrowableOfType(api::outer, GraphQLClientException.class);

        thenDeeplyNestedErrorException(throwable);
    }

    @Test
    void shouldFailToMapOuterNestedError() {
        fixture.returns(deeplyNestedError("{\"outer\":null}"));
        DeepErrorApi api = fixture.build(DeepErrorApi.class);

        GraphQLClientException throwable = catchThrowableOfType(api::outer, GraphQLClientException.class);

        thenDeeplyNestedErrorException(throwable);
    }

    @Test
    void shouldFailToMapInnerNestedError() {
        fixture.returns(deeplyNestedError("{\"outer\":{\"inner\":null}}"));
        DeepErrorApi api = fixture.build(DeepErrorApi.class);

        GraphQLClientException throwable = catchThrowableOfType(api::outer, GraphQLClientException.class);

        thenDeeplyNestedErrorException(throwable);
    }

    private String deeplyNestedError(String data) {
        return "{" +
                "\"data\":" + data + "," +
                "\"errors\":[{" +
                /**/"\"message\":\"dummy message\"," +
                /**/"\"locations\":[{\"line\":1,\"column\":2,\"sourceName\":\"loc\"}]," +
                /**/"\"path\": [\"outer\",\"inner\",\"content\"],\n" +
                /**/"\"extensions\":{\"code\":\"dummy-code\"}" +
                "}]}}";
    }

    private void thenDeeplyNestedErrorException(GraphQLClientException throwable) {
        then(throwable).hasMessage("errors from service")
                .hasExactlyOneErrorWhich()
                .hasMessage("dummy message")
                .hasPath("outer", "inner", "content")
                .hasSourceLocation(1, 2)
                .hasErrorCode("dummy-code");
    }
}
