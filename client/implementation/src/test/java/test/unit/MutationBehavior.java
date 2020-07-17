package test.unit;

import static java.util.Arrays.asList;
import static org.assertj.core.api.BDDAssertions.then;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.eclipse.microprofile.graphql.Mutation;
import org.junit.jupiter.api.Test;

import io.smallrye.graphql.client.typesafe.api.GraphQlClientApi;

public class MutationBehavior {
    private final GraphQlClientFixture fixture = new GraphQlClientFixture();

    @GraphQlClientApi
    interface StringMutationApi {
        @Mutation
        String createSome(String thing);
    }

    @Test
    public void shouldCallStringMutation() {
        fixture.returnsData("'createSome':'output'");
        StringMutationApi api = fixture.builder().build(StringMutationApi.class);

        String greeting = api.createSome("input");

        then(fixture.mutation()).isEqualTo("createSome(thing: 'input')");
        then(greeting).isEqualTo("output");
    }

    @Test
    public void shouldCallNullStringMutation() {
        fixture.returnsData("'createSome':'output'");
        StringMutationApi api = fixture.builder().build(StringMutationApi.class);

        String greeting = api.createSome(null);

        then(fixture.mutation()).isEqualTo("createSome(thing: null)");
        then(greeting).isEqualTo("output");
    }

    @GraphQlClientApi
    interface GreetingMutationApi {
        @Mutation
        Greeting say(Greeting greet);
    }

    private static class Greeting {
        String text;
        int count;

        @SuppressWarnings("unused")
        public Greeting() {
        }

        public Greeting(String text, int count) {
            this.text = text;
            this.count = count;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            Greeting greeting = (Greeting) o;
            return count == greeting.count && text.equals(greeting.text);
        }

        @Override
        public int hashCode() {
            return Objects.hash(text, count);
        }
    }

    @Test
    public void shouldCallGreetingMutation() {
        fixture.returnsData("'say':{'text':'ho','count':3}");
        GreetingMutationApi api = fixture.builder().build(GreetingMutationApi.class);

        Greeting greeting = api.say(new Greeting("hi", 5));

        then(fixture.mutation()).isEqualTo("say(greet: {text: 'hi', count: 5}) {text count}");
        then(greeting).isEqualTo(new Greeting("ho", 3));
    }

    @Test
    public void shouldCallGreetingMutationWithNullValue() {
        fixture.returnsData("'say':{'text':'ho','count':3}");
        GreetingMutationApi api = fixture.builder().build(GreetingMutationApi.class);

        Greeting greeting = api.say(new Greeting(null, 5));

        then(fixture.mutation()).isEqualTo("say(greet: {text: null, count: 5}) {text count}");
        then(greeting).isEqualTo(new Greeting("ho", 3));
    }

    @GraphQlClientApi
    interface GreetingListMutationApi {
        @Mutation
        Greeting say(List<Greeting> greets);
    }

    @Test
    public void shouldCallMutationWithListWithNullValue() {
        fixture.returnsData("'say':{'text':'ho','count':3}");
        GreetingListMutationApi api = fixture.builder().build(GreetingListMutationApi.class);

        Greeting greeting = api.say(asList(
                new Greeting("one", 5),
                null,
                new Greeting("three", 5)));

        then(fixture.mutation()).isEqualTo("say(greets: [" +
                "{text: 'one', count: 5}, " +
                "null, " +
                "{text: 'three', count: 5}" +
                "]) {text count}");
        then(greeting).isEqualTo(new Greeting("ho", 3));
    }

    private static class GreetingContainer {
        Greeting greeting;
        LocalDateTime when;

        @SuppressWarnings("unused")
        GreetingContainer() {
        }

        GreetingContainer(Greeting greeting, LocalDateTime when) {
            this.greeting = greeting;
            this.when = when;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            GreetingContainer that = (GreetingContainer) o;
            return Objects.equals(greeting, that.greeting) &&
                    Objects.equals(when, that.when);
        }

        @Override
        public int hashCode() {
            return Objects.hash(greeting, when);
        }
    }

    @GraphQlClientApi
    interface NestedGreetingMutationApi {
        @Mutation
        Greeting say(GreetingContainer greeting);
    }

    @Test
    public void shouldCallMutationWithNestedValue() {
        fixture.returnsData("'say':{'text':'ho','count':3}");
        NestedGreetingMutationApi api = fixture.builder().build(NestedGreetingMutationApi.class);
        LocalDateTime now = LocalDateTime.now();

        Greeting greeting = api.say(new GreetingContainer(new Greeting("one", 5), now));

        then(fixture.mutation()).isEqualTo("say(greeting: {" +
                "greeting: {text: 'one', count: 5}, " +
                "when: '" + now + "'}) {text count}");
        then(greeting).isEqualTo(new Greeting("ho", 3));
    }

    @Test
    public void shouldCallMutationWithNestedNullValue() {
        fixture.returnsData("'say':{'text':'ho','count':3}");
        NestedGreetingMutationApi api = fixture.builder().build(NestedGreetingMutationApi.class);

        Greeting greeting = api.say(new GreetingContainer(new Greeting(null, 5), null));

        then(fixture.mutation())
                .isEqualTo("say(greeting: {greeting: {text: null, count: 5}, when: null}) {text count}");
        then(greeting).isEqualTo(new Greeting("ho", 3));
    }
}
