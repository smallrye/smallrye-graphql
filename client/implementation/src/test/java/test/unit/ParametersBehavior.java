package test.unit;

import io.smallrye.graphql.client.typesafe.api.GraphQlClientApi;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.assertj.core.api.BDDAssertions.then;

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
    interface BoolParamApi {
        String greeting(boolean really);
    }

    @Test
    public void shouldCallBoolParamQuery() {
        fixture.returnsData("'greeting':'ho'");
        BoolParamApi api = fixture.builder().build(BoolParamApi.class);

        String greeting = api.greeting(true);

        then(fixture.query()).isEqualTo("greeting(really: true)");
        then(greeting).isEqualTo("ho");
    }

    @GraphQlClientApi
    interface BooleanParamApi {
        String greeting(Boolean really);
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

        @SuppressWarnings("unused") Greeting() {
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
        boolean greetings(String[] greets);
    }

    @Test
    public void shouldCallArrayParamQuery() {
        fixture.returnsData("'greetings':true");
        ArrayParamApi api = fixture.builder().build(ArrayParamApi.class);

        boolean success = api.greetings(new String[]{"hi", "ho"});

        then(fixture.query()).isEqualTo("greetings(greets: ['hi', 'ho'])");
        then(success).isTrue();
    }

    @GraphQlClientApi
    interface ListParamApi {
        boolean greetings(List<String> greets);
    }

    @Test
    public void shouldCallListParamQuery() {
        fixture.returnsData("'greetings':true");
        ListParamApi api = fixture.builder().build(ListParamApi.class);

        boolean success = api.greetings(asList("hi", "ho"));

        then(fixture.query()).isEqualTo("greetings(greets: ['hi', 'ho'])");
        then(success).isTrue();
    }

    @GraphQlClientApi
    interface SetParamApi {
        boolean greetings(Set<String> greets);
    }

    @Test
    public void shouldCallSetParamQuery() {
        fixture.returnsData("'greetings':true");
        SetParamApi api = fixture.builder().build(SetParamApi.class);

        boolean success = api.greetings(new HashSet<>(asList("hi", "ho")));

        then(fixture.query()).isEqualTo("greetings(greets: ['hi', 'ho'])");
        then(success).isTrue();
    }

    @GraphQlClientApi
    interface QueueParamApi {
        boolean greetings(Queue<String> greets);
    }

    @Test
    public void shouldCallQueueParamQuery() {
        fixture.returnsData("'greetings':true");
        QueueParamApi api = fixture.builder().build(QueueParamApi.class);

        boolean success = api.greetings(new ArrayDeque<>(asList("hi", "ho")));

        then(fixture.query()).isEqualTo("greetings(greets: ['hi', 'ho'])");
        then(success).isTrue();
    }

    @GraphQlClientApi
    interface CollectionParamApi {
        boolean greetings(Collection<String> greets);
    }

    @Test
    public void shouldCallCollectionParamQuery() {
        fixture.returnsData("'greetings':true");
        CollectionParamApi api = fixture.builder().build(CollectionParamApi.class);

        boolean success = api.greetings(asList("hi", "ho"));

        then(fixture.query()).isEqualTo("greetings(greets: ['hi', 'ho'])");
        then(success).isTrue();
    }

    @GraphQlClientApi
    interface ObjectListParamApi {
        boolean greetings(List<Greeting> greets);
    }

    @Test
    public void shouldCallObjectListParamQuery() {
        fixture.returnsData("'greetings':true");
        ObjectListParamApi api = fixture.builder().build(ObjectListParamApi.class);

        boolean success = api.greetings(asList(new Greeting("hi", 5), new Greeting("ho", 3)));

        then(fixture.query()).isEqualTo("greetings(greets: [{text: 'hi', count: 5}, {text: 'ho', count: 3}])");
        then(success).isTrue();
    }

    @GraphQlClientApi
    interface ListObjectParamApi {
        boolean foo(ListObject bar);
    }

    private static class ListObject {
        List<String> texts;
        int count;

        ListObject(List<String> texts, int count) {
            this.texts = texts;
            this.count = count;
        }
    }

    @Test
    public void shouldCallListObjectParamQuery() {
        fixture.returnsData("'foo':true");
        ListObjectParamApi api = fixture.builder().build(ListObjectParamApi.class);

        boolean success = api.foo(new ListObject(asList("hi", "ho"), 3));

        then(fixture.query()).isEqualTo("foo(bar: {texts: ['hi', 'ho'], count: 3})");
        then(success).isTrue();
    }
}
