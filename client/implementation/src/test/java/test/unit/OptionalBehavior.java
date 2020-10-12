package test.unit;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import io.smallrye.graphql.client.typesafe.api.GraphQlClientApi;

public class OptionalBehavior {
    private final GraphQlClientFixture fixture = new GraphQlClientFixture();

    @GraphQlClientApi
    interface OptionalStringApi {
        Optional<String> greeting();
    }

    @Test
    public void shouldCallNullOptionalStringQuery() {
        fixture.returnsData("'greeting':null");
        OptionalStringApi api = fixture.builder().build(OptionalStringApi.class);

        Optional<String> greeting = api.greeting();

        then(fixture.query()).isEqualTo("query greeting { greeting }");
        then(greeting).isEmpty();
    }

    @Test
    public void shouldCallOptionalStringQuery() {
        fixture.returnsData("'greeting':'hi'");
        OptionalStringApi api = fixture.builder().build(OptionalStringApi.class);

        Optional<String> greeting = api.greeting();

        then(fixture.query()).isEqualTo("query greeting { greeting }");
        then(greeting).contains("hi");
    }

    @GraphQlClientApi
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
    public void shouldCallOptionalGreetingQuery() {
        fixture.returnsData("'greeting':{'text':'hi','code':5}");
        OptionalGreetingApi api = fixture.builder().build(OptionalGreetingApi.class);

        Optional<Greeting> greeting = api.greeting();

        then(fixture.query()).isEqualTo("query greeting { greeting {text code} }");
        then(greeting).contains(new Greeting("hi", 5));
    }

    @Test
    public void shouldCallNullOptionalGreetingQuery() {
        fixture.returnsData("'greeting':null");
        OptionalGreetingApi api = fixture.builder().build(OptionalGreetingApi.class);

        Optional<Greeting> greeting = api.greeting();

        then(fixture.query()).isEqualTo("query greeting { greeting {text code} }");
        then(greeting).isEmpty();
    }

    @GraphQlClientApi
    interface OptionalGreetingListApi {
        Optional<List<Greeting>> greeting();
    }

    @Test
    public void shouldCallOptionalGreetingListQuery() {
        fixture.returnsData("'greeting':[{'text':'hi','code':5},{'text':'ho','code':7}]");
        OptionalGreetingListApi api = fixture.builder().build(OptionalGreetingListApi.class);

        Optional<List<Greeting>> greeting = api.greeting();

        then(fixture.query()).isEqualTo("query greeting { greeting {text code} }");
        assert greeting.isPresent();
        then(greeting.get()).contains(new Greeting("hi", 5), new Greeting("ho", 7));
    }

    @Test
    public void shouldCallEmptyOptionalGreetingListQuery() {
        fixture.returnsData("'greeting':[]");
        OptionalGreetingListApi api = fixture.builder().build(OptionalGreetingListApi.class);

        Optional<List<Greeting>> greeting = api.greeting();

        then(fixture.query()).isEqualTo("query greeting { greeting {text code} }");
        assert greeting.isPresent();
        then(greeting.get()).isEmpty();
    }

    @GraphQlClientApi
    interface ListOfOptionalGreetingApi {
        List<Optional<Greeting>> greetings();
    }

    @Test
    public void shouldCallListOfOptionalGreetingsQuery() {
        fixture.returnsData("'greetings':[{'text':'hi','code':5},{'text':'ho','code':7}]");
        ListOfOptionalGreetingApi api = fixture.builder().build(ListOfOptionalGreetingApi.class);

        List<Optional<Greeting>> greetings = api.greetings();

        then(fixture.query()).isEqualTo("query greetings { greetings {text code} }");
        then(greetings).containsExactly(
                Optional.of(new Greeting("hi", 5)),
                Optional.of(new Greeting("ho", 7)));
    }

    @Test
    public void shouldCallEmptyListOfOptionalGreetingsQuery() {
        fixture.returnsData("'greetings':[]");
        ListOfOptionalGreetingApi api = fixture.builder().build(ListOfOptionalGreetingApi.class);

        List<Optional<Greeting>> greetings = api.greetings();

        then(fixture.query()).isEqualTo("query greetings { greetings {text code} }");
        then(greetings).isEmpty();
    }

    @GraphQlClientApi
    interface OptionalOptionalStringApi {
        Optional<Optional<String>> greeting();
    }

    @Test
    public void shouldCallOptionalOptionalStringQuery() {
        fixture.returnsData("'greeting':'hi'");
        OptionalOptionalStringApi api = fixture.builder().build(OptionalOptionalStringApi.class);

        Optional<Optional<String>> greeting = api.greeting();

        then(fixture.query()).isEqualTo("query greeting { greeting }");
        then(greeting).contains(Optional.of("hi"));
    }
}
