package test.unit;

import static java.util.Arrays.asList;
import static org.assertj.core.api.BDDAssertions.then;

import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.Test;

import io.smallrye.graphql.client.typesafe.api.GraphQlClientApi;

public class ParametersBehavior {
    private final GraphQlClientFixture fixture = new GraphQlClientFixture();

    @GraphQlClientApi
    interface ParamApi {
        String greeting(String who);
    }

    @Test
    public void shouldCallParamQuery() {
        fixture.returnsData("'greeting':'hi, foo'");
        ParamApi api = fixture.builder().build(ParamApi.class);

        String greeting = api.greeting("foo");

        then(fixture.query()).isEqualTo("greeting(who: 'foo')");
        then(greeting).isEqualTo("hi, foo");
    }

    @Test
    public void shouldEscapeParamScalarQuery() {
        fixture.returnsData("'greeting':'hi, foo'");
        ParamApi api = fixture.builder().build(ParamApi.class);

        String greeting = api.greeting("foo\"bar'\n");

        then(fixture.rawQuery()).isEqualTo("greeting(who: \"foo\\\"bar'\\n\")");
        then(greeting).isEqualTo("hi, foo");
    }

    @GraphQlClientApi
    interface ParamsApi {
        String greeting(String who, int count);
    }

    @Test
    public void shouldCallTwoParamsQuery() {
        fixture.returnsData("'greeting':'hi, foo 3'");
        ParamsApi api = fixture.builder().build(ParamsApi.class);

        String greeting = api.greeting("foo", 3);

        then(fixture.query()).isEqualTo("greeting(who: 'foo', count: 3)");
        then(greeting).isEqualTo("hi, foo 3");
    }

    @GraphQlClientApi
    interface BooleanParamApi {
        String greeting(boolean really);
    }

    @Test
    public void shouldCallBooleanParamQuery() {
        fixture.returnsData("'greeting':'ho'");
        BooleanParamApi api = fixture.builder().build(BooleanParamApi.class);

        String greeting = api.greeting(true);

        then(fixture.query()).isEqualTo("greeting(really: true)");
        then(greeting).isEqualTo("ho");
    }

    @GraphQlClientApi
    interface ObjectParamApi {
        Greeting say(Greeting greet);
    }

    private static class Greeting {
        String text;
        int count;

        @SuppressWarnings("unused")
        Greeting() {
        }

        Greeting(String text, int count) {
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
    public void shouldCallObjectParamQuery() {
        fixture.returnsData("'say':{'text':'ho','count':3}");
        ObjectParamApi api = fixture.builder().build(ObjectParamApi.class);

        Greeting greeting = api.say(new Greeting("hi", 5));

        then(fixture.query()).isEqualTo("say(greet: {text: 'hi', count: 5}) {text count}");
        then(greeting).isEqualTo(new Greeting("ho", 3));
    }

    @GraphQlClientApi
    interface ArrayParamApi {
        boolean greetings(List<String> greets);
    }

    @Test
    public void shouldCallArrayParamQuery() {
        fixture.returnsData("'greetings':true");
        ArrayParamApi api = fixture.builder().build(ArrayParamApi.class);

        boolean success = api.greetings(asList("hi", "ho"));

        then(fixture.query()).isEqualTo("greetings(greets: ['hi', 'ho'])");
        then(success).isTrue();
    }

    @GraphQlClientApi
    interface ObjectArrayParamApi {
        boolean greetings(List<Greeting> greets);
    }

    @Test
    public void shouldCallObjectArrayParamQuery() {
        fixture.returnsData("'greetings':true");
        ObjectArrayParamApi api = fixture.builder().build(ObjectArrayParamApi.class);

        boolean success = api.greetings(asList(new Greeting("hi", 5), new Greeting("ho", 3)));

        then(fixture.query()).isEqualTo("greetings(greets: [{text: 'hi', count: 5}, {text: 'ho', count: 3}])");
        then(success).isTrue();
    }

    @GraphQlClientApi
    interface ArrayObjectParamApi {
        boolean foo(ArrayObject bar);
    }

    private static class ArrayObject {
        List<String> texts;
        int count;

        ArrayObject(List<String> texts, int count) {
            this.texts = texts;
            this.count = count;
        }
    }

    @Test
    public void shouldCallArrayObjectParamQuery() {
        fixture.returnsData("'foo':true");
        ArrayObjectParamApi api = fixture.builder().build(ArrayObjectParamApi.class);

        boolean success = api.foo(new ArrayObject(asList("hi", "ho"), 3));

        then(fixture.query()).isEqualTo("foo(bar: {texts: ['hi', 'ho'], count: 3})");
        then(success).isTrue();
    }
}
