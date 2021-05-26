package tck.graphql.typesafe;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import io.smallrye.graphql.client.typesafe.api.GraphQLClientApi;

class OptionalBehavior {
    private final TypesafeGraphQLClientFixture fixture = TypesafeGraphQLClientFixture.load();

    @GraphQLClientApi
    interface OptionalStringApi {
        Optional<String> greeting();
    }

    @Test
    void shouldCallNullOptionalStringQuery() {
        fixture.returnsData("'greeting':null");
        OptionalStringApi api = fixture.build(OptionalStringApi.class);

        Optional<String> greeting = api.greeting();

        then(fixture.query()).isEqualTo("query greeting { greeting }");
        then(greeting).isEmpty();
    }

    @Test
    void shouldCallOptionalStringQuery() {
        fixture.returnsData("'greeting':'hi'");
        OptionalStringApi api = fixture.build(OptionalStringApi.class);

        Optional<String> greeting = api.greeting();

        then(fixture.query()).isEqualTo("query greeting { greeting }");
        then(greeting).contains("hi");
    }

    @GraphQLClientApi
    interface OptionalGreetingApi {
        Optional<Greeting> greeting();
    }

    private static class Greeting {
        String text;
        int code;

        @SuppressWarnings("unused")
        Greeting() {
        }

        Greeting(String text, int code) {
            this.text = text;
            this.code = code;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            Greeting greeting = (Greeting) o;
            return code == greeting.code && text.equals(greeting.text);
        }

        @Override
        public int hashCode() {
            return Objects.hash(text, code);
        }
    }

    @Test
    void shouldCallOptionalGreetingQuery() {
        fixture.returnsData("'greeting':{'text':'hi','code':5}");
        OptionalGreetingApi api = fixture.build(OptionalGreetingApi.class);

        Optional<Greeting> greeting = api.greeting();

        then(fixture.query()).isEqualTo("query greeting { greeting {text code} }");
        then(greeting).contains(new Greeting("hi", 5));
    }

    @Test
    void shouldCallNullOptionalGreetingQuery() {
        fixture.returnsData("'greeting':null");
        OptionalGreetingApi api = fixture.build(OptionalGreetingApi.class);

        Optional<Greeting> greeting = api.greeting();

        then(fixture.query()).isEqualTo("query greeting { greeting {text code} }");
        then(greeting).isEmpty();
    }

    @GraphQLClientApi
    interface OptionalGreetingListApi {
        Optional<List<Greeting>> greeting();
    }

    @Test
    void shouldCallOptionalGreetingListQuery() {
        fixture.returnsData("'greeting':[{'text':'hi','code':5},{'text':'ho','code':7}]");
        OptionalGreetingListApi api = fixture.build(OptionalGreetingListApi.class);

        Optional<List<Greeting>> greeting = api.greeting();

        then(fixture.query()).isEqualTo("query greeting { greeting {text code} }");
        assert greeting.isPresent();
        then(greeting.get()).contains(new Greeting("hi", 5), new Greeting("ho", 7));
    }

    @Test
    void shouldCallEmptyOptionalGreetingListQuery() {
        fixture.returnsData("'greeting':[]");
        OptionalGreetingListApi api = fixture.build(OptionalGreetingListApi.class);

        Optional<List<Greeting>> greeting = api.greeting();

        then(fixture.query()).isEqualTo("query greeting { greeting {text code} }");
        assert greeting.isPresent();
        then(greeting.get()).isEmpty();
    }

    @GraphQLClientApi
    interface ListOfOptionalGreetingApi {
        List<Optional<Greeting>> greetings();
    }

    @Test
    void shouldCallListOfOptionalGreetingsQuery() {
        fixture.returnsData("'greetings':[{'text':'hi','code':5},{'text':'ho','code':7}]");
        ListOfOptionalGreetingApi api = fixture.build(ListOfOptionalGreetingApi.class);

        List<Optional<Greeting>> greetings = api.greetings();

        then(fixture.query()).isEqualTo("query greetings { greetings {text code} }");
        then(greetings).containsExactly(
                Optional.of(new Greeting("hi", 5)),
                Optional.of(new Greeting("ho", 7)));
    }

    @Test
    void shouldCallEmptyListOfOptionalGreetingsQuery() {
        fixture.returnsData("'greetings':[]");
        ListOfOptionalGreetingApi api = fixture.build(ListOfOptionalGreetingApi.class);

        List<Optional<Greeting>> greetings = api.greetings();

        then(fixture.query()).isEqualTo("query greetings { greetings {text code} }");
        then(greetings).isEmpty();
    }

    @GraphQLClientApi
    interface OptionalOptionalStringApi {
        Optional<Optional<String>> greeting();
    }

    @Test
    void shouldCallOptionalOptionalStringQuery() {
        fixture.returnsData("'greeting':'hi'");
        OptionalOptionalStringApi api = fixture.build(OptionalOptionalStringApi.class);

        Optional<Optional<String>> greeting = api.greeting();

        then(fixture.query()).isEqualTo("query greeting { greeting }");
        then(greeting).contains(Optional.of("hi"));
    }
}
