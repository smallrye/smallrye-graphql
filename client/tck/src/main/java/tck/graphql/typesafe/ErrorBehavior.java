package tck.graphql.typesafe;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.assertj.core.api.BDDAssertions.then;

import java.util.List;
import java.util.NoSuchElementException;

import org.eclipse.microprofile.graphql.Name;
import org.junit.jupiter.api.Test;

import io.smallrye.graphql.client.typesafe.api.ErrorOr;
import io.smallrye.graphql.client.typesafe.api.GraphQLClientApi;
import io.smallrye.graphql.client.typesafe.api.GraphQLClientError;
import io.smallrye.graphql.client.typesafe.api.GraphQLClientException;
import io.smallrye.graphql.client.typesafe.api.SourceLocation;

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

        then(thrown).hasMessage("errors from service (and we can't apply them to a java.lang.String value for" +
                " tck.graphql.typesafe.ErrorBehavior$StringApi#greeting; see ErrorOr)");
        then(thrown).hasToString(
                "GraphQlClientException: errors from service (and we can't apply them to a java.lang.String value for "
                        + StringApi.class.getName() + "#greeting; see ErrorOr)\n" +
                        "errors:\n" +
                        "- no-greeting: [greeting] currently can't greet [(1:2@loc)]" +
                        " {description=some description, queryPath=[greeting], classification=DataFetchingException, code=no-greeting})");
        then(thrown.getErrors()).hasSize(1);
        GraphQLClientError error = thrown.getErrors().get(0);
        then(error.getMessage()).isEqualTo("currently can't greet");
        then(error.getLocations()).containsExactly(new SourceLocation(1, 2, "loc"));
        then(error.getPath()).containsExactly("greeting");
        then(error.getErrorCode()).isEqualTo("no-greeting");
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

        then(thrown).hasMessage("errors from service");
        then(thrown).hasToString("GraphQlClientException: errors from service\n" +
                "errors:\n" +
                "- Validation error of type FieldUndefined: Field 'foo' in type 'Query' is undefined @ 'foo' [(1:8)]" +
                " {description=Field 'foo' in type 'Query' is undefined, validationErrorType=FieldUndefined," +
                " queryPath=[foo], classification=ValidationError})");
        then(thrown.getErrors()).hasSize(1);
        GraphQLClientError error = thrown.getErrors().get(0);
        then(error.getMessage())
                .isEqualTo("Validation error of type FieldUndefined: Field 'foo' in type 'Query' is undefined @ 'foo'");
        then(error.getLocations()).containsExactly(new SourceLocation(1, 8, null));
        then(error.getErrorCode()).isNull();
        then(error.getExtensions().get("description")).isEqualTo("Field 'foo' in type 'Query' is undefined");
        then(error.getExtensions().get("validationErrorType")).isEqualTo("FieldUndefined");
        then(error.getExtensions().get("queryPath")).isEqualTo(singletonList("foo"));
        then(error.getExtensions().get("classification")).isEqualTo("ValidationError");
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

        then(thrown).hasMessage("errors from service");
        then(thrown).hasToString("GraphQlClientException: errors from service\n" +
                "errors:\n" +
                "- something went wrong [(1:8)])");
        then(thrown.getErrors()).hasSize(1);
        GraphQLClientError error = thrown.getErrors().get(0);
        then(error.getMessage()).isEqualTo("something went wrong");
        then(error.getLocations()).containsExactly(new SourceLocation(1, 8, null));
        then(error.getErrorCode()).isNull();
        then(error.getExtensions()).isNull();
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

        then(thrown).hasMessage("errors from service");
        then(thrown).hasToString("GraphQlClientException: errors from service\n" +
                "errors:\n" +
                "- something went wrong {classification=SomeClassification})");
        then(thrown.getErrors()).hasSize(1);
        GraphQLClientError error = thrown.getErrors().get(0);
        then(error.getMessage()).isEqualTo("something went wrong");
        then(error.getLocations()).isNull();
        then(error.getErrorCode()).isNull();
        then(error.getExtensions().get("classification")).isEqualTo("SomeClassification");
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

        then(thrown).hasMessage("errors from service");
        then(thrown).hasToString("GraphQlClientException: errors from service\n" +
                "errors:\n" +
                "- something went wrong [] {classification=SomeClassification})");
        then(thrown.getErrors()).hasSize(1);
        GraphQLClientError error = thrown.getErrors().get(0);
        then(error.getMessage()).isEqualTo("something went wrong");
        then(error.getLocations()).isEmpty();
        then(error.getErrorCode()).isNull();
        then(error.getExtensions().get("classification")).isEqualTo("SomeClassification");
    }

    @Test
    void shouldFailStringQueryNotFound() {
        fixture.returnsServerError();
        StringApi api = fixture.build(StringApi.class);

        GraphQLClientException thrown = catchThrowableOfType(api::greeting, GraphQLClientException.class);

        then(thrown).hasMessage("expected successful status code but got 500 Internal Server Error:\nfailed");
    }

    @Test
    void shouldFailOnMissingQueryResponse() {
        fixture.returnsData("");
        StringApi api = fixture.build(StringApi.class);

        GraphQLClientException thrown = catchThrowableOfType(api::greeting, GraphQLClientException.class);

        then(thrown).hasMessage("no data for 'greeting':\n  {}");
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
        then(response.isError()).isFalse();
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
        then(response.isError()).isTrue();
        then(response.isPresent()).isFalse();
        then(catchThrowable(response::get)).isInstanceOf(NoSuchElementException.class);
        then(response.getErrors()).hasSize(1);
        GraphQLClientError error = response.getErrors().get(0);
        then(error.getMessage()).isEqualTo("currently can't search for teams");
        then(error.getPath()).containsExactly("teams");
        then(error.getLocations()).containsExactly(new SourceLocation(1, 2, "loc"));
        then(error.getErrorCode()).isEqualTo("team-search-disabled");
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
        then(throwable).hasMessage("errors from service");
        then(throwable).hasToString("GraphQlClientException: errors from service\n" +
                "errors:\n" +
                "- team-search-disabled: [teams] currently can't search for teams [(1:2@loc)]" +
                " {description=Field 'foo' in type 'Query' is undefined, validationErrorType=FieldUndefined, queryPath=[foo]," +
                " classification=ValidationError, code=team-search-disabled})");
        then(throwable.getErrors()).hasSize(1);
        GraphQLClientError error = throwable.getErrors().get(0);
        then(error.getMessage()).isEqualTo("currently can't search for teams");
        then(error.getPath()).containsExactly("teams");
        then(error.getLocations()).containsExactly(new SourceLocation(1, 2, "loc"));
        then(error.getErrorCode()).isEqualTo("team-search-disabled");
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
        then(response.isError()).isTrue();
        then(response.isPresent()).isFalse();
        then(catchThrowable(response::get)).isInstanceOf(NoSuchElementException.class);

        then(response.getErrors()).hasSize(2);
        GraphQLClientError error1 = response.getErrors().get(0);
        then(error1.getMessage()).isEqualTo("currently can't search for teams");
        then(error1.getPath()).containsExactly("teams");
        then(error1.getLocations()).containsExactly(new SourceLocation(1, 2, "loc"));
        then(error1.getErrorCode()).isEqualTo("team-search-disabled");

        GraphQLClientError error2 = response.getErrors().get(1);
        then(error2.getMessage()).isEqualTo("feeling dizzy");
        then(error2.getPath()).containsExactly("teams");
        then(error2.getLocations()).containsExactly(new SourceLocation(2, 3, "lock"));
        then(error2.getErrorCode()).isEqualTo("dizzy");
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
        then(throwable).hasMessage("errors from service");
        then(throwable).hasToString("GraphQlClientException: errors from service\n" +
                "errors:\n" +
                "- team-search-disabled: currently can't search for teams [(1:2@loc)]" +
                " {description=Field 'foo' in type 'Query' is undefined, validationErrorType=FieldUndefined, queryPath=[foo]," +
                " classification=ValidationError, code=team-search-disabled})");
        then(throwable.getErrors()).hasSize(1);
        GraphQLClientError error = throwable.getErrors().get(0);
        then(error.getMessage()).isEqualTo("currently can't search for teams");
        then(error.getPath()).isNull();
        then(error.getLocations()).containsExactly(new SourceLocation(1, 2, "loc"));
        then(error.getErrorCode()).isEqualTo("team-search-disabled");
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
            then(superHero.isError()).isFalse();
            then(superHero.get().name).isEqualTo("Spider-Man");
            then(superHero.get().location.isPresent()).isTrue();
            then(superHero.get().location.isError()).isFalse();
            then(superHero.get().location.get()).isEqualTo("New York");
        }
        {
            then(response.teams.isPresent()).isTrue();
            then(response.teams.isError()).isFalse();
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
        then(response.teams.isError()).isTrue();
        then(response.teams.isPresent()).isFalse();
        then(catchThrowable(() -> response.teams.get())).isInstanceOf(NoSuchElementException.class);
        then(response.teams.getErrors()).hasSize(1);
        GraphQLClientError error = response.teams.getErrors().get(0);
        then(error.getMessage()).isEqualTo("currently can't search for teams");
        then(error.getLocations()).containsExactly(new SourceLocation(1, 2, "loc"));
        then(error.getPath()).containsExactly("find", "teams");
        then(error.getErrorCode()).isEqualTo("team-search-disabled");
    }
}
