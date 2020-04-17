package test.unit;

import static java.util.Arrays.asList;
import static org.assertj.core.api.BDDAssertions.then;

import java.util.List;

import org.junit.jupiter.api.Test;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

class ParametersBehavior {
    private final GraphQlClientFixture fixture = new GraphQlClientFixture();

    interface ParamApi {
        String greeting(String who);
    }

    @Test
    void shouldCallParamQuery() {
        fixture.returnsData("'greeting':'hi, foo'");
        ParamApi api = fixture.builder().build(ParamApi.class);

        String greeting = api.greeting("foo");

        then(fixture.query()).isEqualTo("greeting(who: 'foo')");
        then(greeting).isEqualTo("hi, foo");
    }

    @Test
    void shouldEscapeParamScalarQuery() {
        fixture.returnsData("'greeting':'hi, foo'");
        ParamApi api = fixture.builder().build(ParamApi.class);

        String greeting = api.greeting("foo\"bar'\n");

        then(fixture.rawQuery()).isEqualTo("greeting(who: \"foo\\\"bar'\\n\")");
        then(greeting).isEqualTo("hi, foo");
    }

    interface ParamsApi {
        String greeting(String who, int count);
    }

    @Test
    void shouldCallTwoParamsQuery() {
        fixture.returnsData("'greeting':'hi, foo 3'");
        ParamsApi api = fixture.builder().build(ParamsApi.class);

        String greeting = api.greeting("foo", 3);

        then(fixture.query()).isEqualTo("greeting(who: 'foo', count: 3)");
        then(greeting).isEqualTo("hi, foo 3");
    }

    interface BooleanParamApi {
        String greeting(boolean really);
    }

    @Test
    void shouldCallBooleanParamQuery() {
        fixture.returnsData("'greeting':'ho'");
        BooleanParamApi api = fixture.builder().build(BooleanParamApi.class);

        String greeting = api.greeting(true);

        then(fixture.query()).isEqualTo("greeting(really: true)");
        then(greeting).isEqualTo("ho");
    }

    interface ObjectParamApi {
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
    void shouldCallObjectParamQuery() {
        fixture.returnsData("'say':{'text':'ho','count':3}");
        ObjectParamApi api = fixture.builder().build(ObjectParamApi.class);

        Greeting greeting = api.say(new Greeting("hi", 5));

        then(fixture.query()).isEqualTo("say(greet: {text: 'hi', count: 5}) {text count}");
        then(greeting).isEqualTo(new Greeting("ho", 3));
    }

    interface ArrayParamApi {
        boolean greetings(List<String> greets);
    }

    @Test
    void shouldCallArrayParamQuery() {
        fixture.returnsData("'greetings':true");
        ArrayParamApi api = fixture.builder().build(ArrayParamApi.class);

        boolean success = api.greetings(asList("hi", "ho"));

        then(fixture.query()).isEqualTo("greetings(greets: ['hi', 'ho'])");
        then(success).isTrue();
    }

    interface ObjectArrayParamApi {
        boolean greetings(List<Greeting> greets);
    }

    @Test
    void shouldCallObjectArrayParamQuery() {
        fixture.returnsData("'greetings':true");
        ObjectArrayParamApi api = fixture.builder().build(ObjectArrayParamApi.class);

        boolean success = api.greetings(asList(new Greeting("hi", 5), new Greeting("ho", 3)));

        then(fixture.query()).isEqualTo("greetings(greets: [{text: 'hi', count: 5}, {text: 'ho', count: 3}])");
        then(success).isTrue();
    }

    interface ArrayObjectParamApi {
        boolean foo(ArrayObject bar);
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    static class ArrayObject {
        List<String> texts;
        int count;
    }

    @Test
    void shouldCallArrayObjectParamQuery() {
        fixture.returnsData("'foo':true");
        ArrayObjectParamApi api = fixture.builder().build(ArrayObjectParamApi.class);

        boolean success = api.foo(new ArrayObject(asList("hi", "ho"), 3));

        then(fixture.query()).isEqualTo("foo(bar: {texts: ['hi', 'ho'], count: 3})");
        then(success).isTrue();
    }

    // TODO params as variables?
}
