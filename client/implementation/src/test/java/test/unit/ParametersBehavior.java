package test.unit;

import static java.util.Arrays.asList;
import static org.assertj.core.api.BDDAssertions.then;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;

import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.NonNull;
import org.junit.jupiter.api.Test;

import io.smallrye.graphql.client.typesafe.api.GraphQlClientApi;

public class ParametersBehavior {
    private final GraphQlClientFixture fixture = new GraphQlClientFixture();

    @GraphQlClientApi
    interface StringParamApi {
        String greeting(String who);
    }

    @Test
    public void shouldCallStringParamQuery() {
        fixture.returnsData("'greeting':'hi, foo'");
        StringParamApi api = fixture.builder().build(StringParamApi.class);

        String greeting = api.greeting("foo");

        then(fixture.query()).isEqualTo("query greeting($who: String) { greeting(who: $who) }");
        then(fixture.variables()).isEqualTo("{'who':'foo'}");
        then(fixture.operationName()).isEqualTo("greeting");
        then(greeting).isEqualTo("hi, foo");
    }

    @GraphQlClientApi
    interface NonNullStringParamApi {
        String greeting(@NonNull String who);
    }

    @Test
    public void shouldCallNonNullStringParamQuery() {
        fixture.returnsData("'greeting':'hi, foo'");
        NonNullStringParamApi api = fixture.builder().build(NonNullStringParamApi.class);

        String greeting = api.greeting("foo");

        then(fixture.query()).isEqualTo("query greeting($who: String!) { greeting(who: $who) }");
        then(fixture.variables()).isEqualTo("{'who':'foo'}");
        then(fixture.operationName()).isEqualTo("greeting");
        then(greeting).isEqualTo("hi, foo");
    }

    @GraphQlClientApi
    interface RenamedParamApi {
        String greeting(@Name("who") String foo);
    }

    @Test
    public void shouldCallRenamedParamQuery() {
        fixture.returnsData("'greeting':'hi, foo'");
        RenamedParamApi api = fixture.builder().build(RenamedParamApi.class);

        String greeting = api.greeting("foo");

        then(fixture.query()).isEqualTo("query greeting($who: String) { greeting(who: $who) }");
        then(fixture.variables()).isEqualTo("{'who':'foo'}");
        then(fixture.operationName()).isEqualTo("greeting");
        then(greeting).isEqualTo("hi, foo");
    }

    @Test
    public void shouldEscapeParamScalarQuery() {
        fixture.returnsData("'greeting':'hi, foo'");
        StringParamApi api = fixture.builder().build(StringParamApi.class);

        String greeting = api.greeting("foo\"bar'\n");

        then(fixture.query()).isEqualTo("query greeting($who: String) { greeting(who: $who) }");
        then(fixture.rawVariables()).isEqualTo("{\"who\":\"foo\\\"bar'\\n\"}");
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

        then(fixture.query()).isEqualTo("query greeting($who: String, $count: Int!) { greeting(who: $who, count: $count) }");
        then(fixture.variables()).isEqualTo("{'who':'foo','count':3}");
        then(greeting).isEqualTo("hi, foo 3");
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

        then(fixture.query()).isEqualTo("query say($greet: Greeting) { say(greet: $greet) {text count} }");
        then(fixture.variables()).isEqualTo("{'greet':{'text':'hi','count':5}}");
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

        boolean success = api.greetings(new String[]{ "hi", "ho" });

        then(fixture.query()).isEqualTo("query greetings($greets: [String]) { greetings(greets: $greets) }");
        then(fixture.variables()).isEqualTo("{'greets':['hi','ho']}");
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

        then(fixture.query()).isEqualTo("query greetings($greets: [String]) { greetings(greets: $greets) }");
        then(fixture.variables()).isEqualTo("{'greets':['hi','ho']}");
        then(success).isTrue();
    }

    @GraphQlClientApi
    interface NonNullListParamApi {
        boolean greetings(@NonNull List<String> greets);
    }

    @Test
    public void shouldCallNonNullListParamQuery() {
        fixture.returnsData("'greetings':true");
        NonNullListParamApi api = fixture.builder().build(NonNullListParamApi.class);

        boolean success = api.greetings(asList("hi", "ho"));

        then(fixture.query()).isEqualTo("query greetings($greets: [String]!) { greetings(greets: $greets) }");
        then(fixture.variables()).isEqualTo("{'greets':['hi','ho']}");
        then(success).isTrue();
    }

    @GraphQlClientApi
    interface ListOfNonNullParamApi {
        boolean greetings(List<@NonNull String> greets);
    }

    @Test
    public void shouldCallListOfNonNullParamQuery() {
        fixture.returnsData("'greetings':true");
        ListOfNonNullParamApi api = fixture.builder().build(ListOfNonNullParamApi.class);

        boolean success = api.greetings(asList("hi", "ho"));

        then(fixture.query()).isEqualTo("query greetings($greets: [String!]) { greetings(greets: $greets) }");
        then(fixture.variables()).isEqualTo("{'greets':['hi','ho']}");
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

        then(fixture.query()).isEqualTo("query greetings($greets: [String]) { greetings(greets: $greets) }");
        then(fixture.variables()).isEqualTo("{'greets':['hi','ho']}");
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

        then(fixture.query()).isEqualTo("query greetings($greets: [String]) { greetings(greets: $greets) }");
        then(fixture.variables()).isEqualTo("{'greets':['hi','ho']}");
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

        then(fixture.query()).isEqualTo("query greetings($greets: [String]) { greetings(greets: $greets) }");
        then(fixture.variables()).isEqualTo("{'greets':['hi','ho']}");
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

        then(fixture.query()).isEqualTo("query greetings($greets: [Greeting]) { greetings(greets: $greets) }");
        then(fixture.variables()).isEqualTo("{'greets':[{'text':'hi','count':5},{'text':'ho','count':3}]}");
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

        then(fixture.query()).isEqualTo("query foo($bar: ListObject) { foo(bar: $bar) }");
        then(fixture.variables()).isEqualTo("{'bar':{'texts':['hi','ho'],'count':3}}");
        then(success).isTrue();
    }

    @GraphQlClientApi
    interface BooleanApi {
        String greeting(Boolean who);
    }

    @Test
    public void shouldCallBooleanQuery() {
        fixture.returnsData("'greeting':'hi, foo'");
        BooleanApi api = fixture.builder().build(BooleanApi.class);

        String greeting = api.greeting(true);

        then(fixture.query()).isEqualTo("query greeting($who: Boolean) { greeting(who: $who) }");
        then(fixture.variables()).isEqualTo("{'who':true}");
        then(fixture.operationName()).isEqualTo("greeting");
        then(greeting).isEqualTo("hi, foo");
    }

    @GraphQlClientApi
    interface PrimitiveBooleanApi {
        String greeting(boolean who);
    }

    @Test
    public void shouldCallPrimitiveBooleanQuery() {
        fixture.returnsData("'greeting':'hi, foo'");
        PrimitiveBooleanApi api = fixture.builder().build(PrimitiveBooleanApi.class);

        String greeting = api.greeting(false);

        then(fixture.query()).isEqualTo("query greeting($who: Boolean!) { greeting(who: $who) }");
        then(fixture.variables()).isEqualTo("{'who':false}");
        then(fixture.operationName()).isEqualTo("greeting");
        then(greeting).isEqualTo("hi, foo");
    }

    @GraphQlClientApi
    interface NonNullBooleanApi {
        String greeting(@NonNull Boolean who);
    }

    @Test
    public void shouldCallNonNullBooleanQuery() {
        fixture.returnsData("'greeting':'hi, foo'");
        NonNullBooleanApi api = fixture.builder().build(NonNullBooleanApi.class);

        String greeting = api.greeting(true);

        then(fixture.query()).isEqualTo("query greeting($who: Boolean!) { greeting(who: $who) }");
        then(fixture.variables()).isEqualTo("{'who':true}");
        then(fixture.operationName()).isEqualTo("greeting");
        then(greeting).isEqualTo("hi, foo");
    }

    @GraphQlClientApi
    interface IntegerApi {
        String greeting(Integer who);
    }

    @Test
    public void shouldCallIntegerQuery() {
        fixture.returnsData("'greeting':'hi, foo'");
        IntegerApi api = fixture.builder().build(IntegerApi.class);

        String greeting = api.greeting(123);

        then(fixture.query()).isEqualTo("query greeting($who: Int) { greeting(who: $who) }");
        then(fixture.variables()).isEqualTo("{'who':123}");
        then(fixture.operationName()).isEqualTo("greeting");
        then(greeting).isEqualTo("hi, foo");
    }

    @GraphQlClientApi
    interface PrimitiveIntegerApi {
        String greeting(int who);
    }

    @Test
    public void shouldCallPrimitiveIntegerQuery() {
        fixture.returnsData("'greeting':'hi, foo'");
        PrimitiveIntegerApi api = fixture.builder().build(PrimitiveIntegerApi.class);

        String greeting = api.greeting(-12);

        then(fixture.query()).isEqualTo("query greeting($who: Int!) { greeting(who: $who) }");
        then(fixture.variables()).isEqualTo("{'who':-12}");
        then(fixture.operationName()).isEqualTo("greeting");
        then(greeting).isEqualTo("hi, foo");
    }

    @GraphQlClientApi
    interface NonNullIntegerApi {
        String greeting(@NonNull Integer who);
    }

    @Test
    public void shouldCallNonNullIntegerQuery() {
        fixture.returnsData("'greeting':'hi, foo'");
        NonNullIntegerApi api = fixture.builder().build(NonNullIntegerApi.class);

        String greeting = api.greeting(456);

        then(fixture.query()).isEqualTo("query greeting($who: Int!) { greeting(who: $who) }");
        then(fixture.variables()).isEqualTo("{'who':456}");
        then(fixture.operationName()).isEqualTo("greeting");
        then(greeting).isEqualTo("hi, foo");
    }

    @GraphQlClientApi
    interface LongApi {
        String greeting(Long who);
    }

    @Test
    public void shouldCallLongQuery() {
        fixture.returnsData("'greeting':'hi, foo'");
        LongApi api = fixture.builder().build(LongApi.class);

        String greeting = api.greeting(123L);

        then(fixture.query()).isEqualTo("query greeting($who: Int) { greeting(who: $who) }");
        then(fixture.variables()).isEqualTo("{'who':123}");
        then(fixture.operationName()).isEqualTo("greeting");
        then(greeting).isEqualTo("hi, foo");
    }

    @GraphQlClientApi
    interface PrimitiveLongApi {
        String greeting(long who);
    }

    @Test
    public void shouldCallPrimitiveLongQuery() {
        fixture.returnsData("'greeting':'hi, foo'");
        PrimitiveLongApi api = fixture.builder().build(PrimitiveLongApi.class);

        String greeting = api.greeting(123L);

        then(fixture.query()).isEqualTo("query greeting($who: Int!) { greeting(who: $who) }");
        then(fixture.variables()).isEqualTo("{'who':123}");
        then(fixture.operationName()).isEqualTo("greeting");
        then(greeting).isEqualTo("hi, foo");
    }

    @GraphQlClientApi
    interface NonNullLongApi {
        String greeting(@NonNull Long who);
    }

    @Test
    public void shouldCallNonNullLongQuery() {
        fixture.returnsData("'greeting':'hi, foo'");
        NonNullLongApi api = fixture.builder().build(NonNullLongApi.class);

        String greeting = api.greeting(123L);

        then(fixture.query()).isEqualTo("query greeting($who: Int!) { greeting(who: $who) }");
        then(fixture.variables()).isEqualTo("{'who':123}");
        then(fixture.operationName()).isEqualTo("greeting");
        then(greeting).isEqualTo("hi, foo");
    }
}
