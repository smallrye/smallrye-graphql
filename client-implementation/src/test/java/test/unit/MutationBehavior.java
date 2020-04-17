package test.unit;

import static org.assertj.core.api.BDDAssertions.then;

import org.eclipse.microprofile.graphql.Mutation;
import org.junit.jupiter.api.Test;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

class MutationBehavior {
    private final GraphQlClientFixture fixture = new GraphQlClientFixture();

    interface StringMutationApi {
        @Mutation
        String createSome(String thing);
    }

    @Test
    void shouldCallStringMutation() {
        fixture.returnsData("'createSome':'output'");
        StringMutationApi api = fixture.builder().build(StringMutationApi.class);

        String greeting = api.createSome("input");

        then(fixture.mutation()).isEqualTo("createSome(thing: 'input')");
        then(greeting).isEqualTo("output");
    }

    interface GreetingMutationApi {
        @Mutation
        Greeting say(Greeting greet);
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    static class Greeting {
        String text;
        int count;
    }

    @Test
    void shouldCallGreetingMutation() {
        fixture.returnsData("'say':{'text':'ho','count':3}");
        GreetingMutationApi api = fixture.builder().build(GreetingMutationApi.class);

        Greeting greeting = api.say(new Greeting("hi", 5));

        then(fixture.mutation()).isEqualTo("say(greet: {text: 'hi', count: 5}) {text count}");
        then(greeting).isEqualTo(new Greeting("ho", 3));
    }
}
