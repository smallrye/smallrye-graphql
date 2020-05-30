package test.unit;

import static org.assertj.core.api.BDDAssertions.then;

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
}
