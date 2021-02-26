package test.unit;

import static java.util.Arrays.asList;
import static org.assertj.core.api.BDDAssertions.then;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.eclipse.microprofile.graphql.Input;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.NonNull;
import org.eclipse.microprofile.graphql.Type;
import org.junit.jupiter.api.Test;

import io.smallrye.graphql.client.typesafe.api.GraphQlClientApi;

class ParametersBehavior {
    private final GraphQlClientFixture fixture = new GraphQlClientFixture();

    @GraphQlClientApi
    interface StringParamApi {
        String greeting(String who);
    }

    @Test
    void shouldCallStringParamQuery() {
        fixture.returnsData("'greeting':'hi, foo'");
        StringParamApi api = fixture.build(StringParamApi.class);

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
    void shouldCallNonNullStringParamQuery() {
        fixture.returnsData("'greeting':'hi, foo'");
        NonNullStringParamApi api = fixture.build(NonNullStringParamApi.class);

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
    void shouldCallRenamedParamQuery() {
        fixture.returnsData("'greeting':'hi, foo'");
        RenamedParamApi api = fixture.build(RenamedParamApi.class);

        String greeting = api.greeting("foo");

        then(fixture.query()).isEqualTo("query greeting($who: String) { greeting(who: $who) }");
        then(fixture.variables()).isEqualTo("{'who':'foo'}");
        then(fixture.operationName()).isEqualTo("greeting");
        then(greeting).isEqualTo("hi, foo");
    }

    @Test
    void shouldEscapeParamScalarQuery() {
        fixture.returnsData("'greeting':'hi, foo'");
        StringParamApi api = fixture.build(StringParamApi.class);

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
    void shouldCallTwoParamsQuery() {
        fixture.returnsData("'greeting':'hi, foo 3'");
        ParamsApi api = fixture.build(ParamsApi.class);

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

        @SuppressWarnings("unused")
        Greeting() {
        }

        Greeting(String text, int count) {
            this.text = text;
            this.count = count;
        }
    }

    @Test
    void shouldCallObjectParamQuery() {
        fixture.returnsData("'say':{'text':'ho','count':3}");
        ObjectParamApi api = fixture.build(ObjectParamApi.class);

        Greeting greeting = api.say(new Greeting("hi", 5));

        then(fixture.query()).isEqualTo("query say($greet: GreetingInput) { say(greet: $greet) {text count} }");
        then(fixture.variables()).isEqualTo("{'greet':{'text':'hi','count':5}}");
        then(greeting.text).isEqualTo("ho");
        then(greeting.count).isEqualTo(3);
    }

    @GraphQlClientApi
    interface RenamedObjectParamApi {
        RenamedGreeting say(RenamedGreeting greet);
    }

    @Name("Greeting")
    private static class RenamedGreeting {
        String text;
        int count;

        @SuppressWarnings("unused")
        RenamedGreeting() {
        }

        RenamedGreeting(String text, int count) {
            this.text = text;
            this.count = count;
        }
    }

    @Test
    void shouldCallRenamedObjectParamQuery() {
        fixture.returnsData("'say':{'text':'ho','count':3}");
        RenamedObjectParamApi api = fixture.build(RenamedObjectParamApi.class);

        RenamedGreeting greeting = api.say(new RenamedGreeting("hi", 5));

        then(fixture.query()).isEqualTo("query say($greet: Greeting) { say(greet: $greet) {text count} }");
        then(fixture.variables()).isEqualTo("{'greet':{'text':'hi','count':5}}");
        then(greeting.text).isEqualTo("ho");
        then(greeting.count).isEqualTo(3);
    }

    @GraphQlClientApi
    interface NamedInputObjectParamApi {
        NamedInputGreeting say(NamedInputGreeting greet);
    }

    @Input("Greet")
    private static class NamedInputGreeting {
        String text;
        int count;

        @SuppressWarnings("unused")
        NamedInputGreeting() {
        }

        NamedInputGreeting(String text, int count) {
            this.text = text;
            this.count = count;
        }
    }

    @Test
    void shouldCallNamedInputObjectParamQuery() {
        fixture.returnsData("'say':{'text':'ho','count':3}");
        NamedInputObjectParamApi api = fixture.build(NamedInputObjectParamApi.class);

        NamedInputGreeting greeting = api.say(new NamedInputGreeting("hi", 5));

        then(fixture.query()).isEqualTo("query say($greet: Greet) { say(greet: $greet) {text count} }");
        then(fixture.variables()).isEqualTo("{'greet':{'text':'hi','count':5}}");
        then(greeting.text).isEqualTo("ho");
        then(greeting.count).isEqualTo(3);
    }

    @GraphQlClientApi
    interface EmptyInputObjectParamApi {
        EmptyInputGreeting say(EmptyInputGreeting greet);
    }

    @Input
    private static class EmptyInputGreeting {
        String text;
        int count;

        @SuppressWarnings("unused")
        EmptyInputGreeting() {
        }

        EmptyInputGreeting(String text, int count) {
            this.text = text;
            this.count = count;
        }
    }

    @Test
    void shouldCallEmptyInputObjectParamQuery() {
        fixture.returnsData("'say':{'text':'ho','count':3}");
        EmptyInputObjectParamApi api = fixture.build(EmptyInputObjectParamApi.class);

        EmptyInputGreeting greeting = api.say(new EmptyInputGreeting("hi", 5));

        then(fixture.query()).isEqualTo("query say($greet: EmptyInputGreetingInput) { say(greet: $greet) {text count} }");
        then(fixture.variables()).isEqualTo("{'greet':{'text':'hi','count':5}}");
        then(greeting.text).isEqualTo("ho");
        then(greeting.count).isEqualTo(3);
    }

    @GraphQlClientApi
    interface NamedTypeObjectParamApi {
        NamedTypeGreeting say(NamedTypeGreeting greet);
    }

    @Type("Greet")
    private static class NamedTypeGreeting {
        String text;
        int count;

        @SuppressWarnings("unused")
        NamedTypeGreeting() {
        }

        NamedTypeGreeting(String text, int count) {
            this.text = text;
            this.count = count;
        }
    }

    @Test
    void shouldCallNamedTypeObjectParamQuery() {
        fixture.returnsData("'say':{'text':'ho','count':3}");
        NamedTypeObjectParamApi api = fixture.build(NamedTypeObjectParamApi.class);

        NamedTypeGreeting greeting = api.say(new NamedTypeGreeting("hi", 5));

        then(fixture.query()).isEqualTo("query say($greet: NamedTypeGreetingInput) { say(greet: $greet) {text count} }");
        then(fixture.variables()).isEqualTo("{'greet':{'text':'hi','count':5}}");
        then(greeting.text).isEqualTo("ho");
        then(greeting.count).isEqualTo(3);
    }

    @GraphQlClientApi
    interface TypeAndInputObjectParamApi {
        TypeAndInputGreeting say(TypeAndInputGreeting greet);
    }

    @Type("Gretel")
    @Input("Greet")
    private static class TypeAndInputGreeting {
        String text;
        int count;

        @SuppressWarnings("unused")
        TypeAndInputGreeting() {
        }

        TypeAndInputGreeting(String text, int count) {
            this.text = text;
            this.count = count;
        }
    }

    @Test
    void shouldCallTypeAndInputObjectParamQuery() {
        fixture.returnsData("'say':{'text':'ho','count':3}");
        TypeAndInputObjectParamApi api = fixture.build(TypeAndInputObjectParamApi.class);

        TypeAndInputGreeting greeting = api.say(new TypeAndInputGreeting("hi", 5));

        then(fixture.query()).isEqualTo("query say($greet: Greet) { say(greet: $greet) {text count} }");
        then(fixture.variables()).isEqualTo("{'greet':{'text':'hi','count':5}}");
        then(greeting.text).isEqualTo("ho");
        then(greeting.count).isEqualTo(3);
    }

    @GraphQlClientApi
    interface ArrayParamApi {
        boolean greetings(String[] greets);
    }

    @Test
    void shouldCallArrayParamQuery() {
        fixture.returnsData("'greetings':true");
        ArrayParamApi api = fixture.build(ArrayParamApi.class);

        boolean success = api.greetings(new String[] { "hi", "ho" });

        then(fixture.query()).isEqualTo("query greetings($greets: [String]) { greetings(greets: $greets) }");
        then(fixture.variables()).isEqualTo("{'greets':['hi','ho']}");
        then(success).isTrue();
    }

    @GraphQlClientApi
    interface ListParamApi {
        boolean greetings(List<String> greets);
    }

    @Test
    void shouldCallListParamQuery() {
        fixture.returnsData("'greetings':true");
        ListParamApi api = fixture.build(ListParamApi.class);

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
    void shouldCallNonNullListParamQuery() {
        fixture.returnsData("'greetings':true");
        NonNullListParamApi api = fixture.build(NonNullListParamApi.class);

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
    void shouldCallListOfNonNullParamQuery() {
        fixture.returnsData("'greetings':true");
        ListOfNonNullParamApi api = fixture.build(ListOfNonNullParamApi.class);

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
    void shouldCallSetParamQuery() {
        fixture.returnsData("'greetings':true");
        SetParamApi api = fixture.build(SetParamApi.class);

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
    void shouldCallQueueParamQuery() {
        fixture.returnsData("'greetings':true");
        QueueParamApi api = fixture.build(QueueParamApi.class);

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
    void shouldCallCollectionParamQuery() {
        fixture.returnsData("'greetings':true");
        CollectionParamApi api = fixture.build(CollectionParamApi.class);

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
    void shouldCallObjectListParamQuery() {
        fixture.returnsData("'greetings':true");
        ObjectListParamApi api = fixture.build(ObjectListParamApi.class);

        boolean success = api.greetings(asList(new Greeting("hi", 5), new Greeting("ho", 3)));

        then(fixture.query()).isEqualTo("query greetings($greets: [GreetingInput]) { greetings(greets: $greets) }");
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
    void shouldCallListObjectParamQuery() {
        fixture.returnsData("'foo':true");
        ListObjectParamApi api = fixture.build(ListObjectParamApi.class);

        boolean success = api.foo(new ListObject(asList("hi", "ho"), 3));

        then(fixture.query()).isEqualTo("query foo($bar: ListObjectInput) { foo(bar: $bar) }");
        then(fixture.variables()).isEqualTo("{'bar':{'texts':['hi','ho'],'count':3}}");
        then(success).isTrue();
    }

    @GraphQlClientApi
    interface BooleanApi {
        String greeting(Boolean who);
    }

    @Test
    void shouldCallBooleanQuery() {
        fixture.returnsData("'greeting':'hi, foo'");
        BooleanApi api = fixture.build(BooleanApi.class);

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
    void shouldCallPrimitiveBooleanQuery() {
        fixture.returnsData("'greeting':'hi, foo'");
        PrimitiveBooleanApi api = fixture.build(PrimitiveBooleanApi.class);

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
    void shouldCallNonNullBooleanQuery() {
        fixture.returnsData("'greeting':'hi, foo'");
        NonNullBooleanApi api = fixture.build(NonNullBooleanApi.class);

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
    void shouldCallIntegerQuery() {
        fixture.returnsData("'greeting':'hi, foo'");
        IntegerApi api = fixture.build(IntegerApi.class);

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
    void shouldCallPrimitiveIntegerQuery() {
        fixture.returnsData("'greeting':'hi, foo'");
        PrimitiveIntegerApi api = fixture.build(PrimitiveIntegerApi.class);

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
    void shouldCallNonNullIntegerQuery() {
        fixture.returnsData("'greeting':'hi, foo'");
        NonNullIntegerApi api = fixture.build(NonNullIntegerApi.class);

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
    void shouldCallLongQuery() {
        fixture.returnsData("'greeting':'hi, foo'");
        LongApi api = fixture.build(LongApi.class);

        String greeting = api.greeting(123L);

        then(fixture.query()).isEqualTo("query greeting($who: BigInteger) { greeting(who: $who) }");
        then(fixture.variables()).isEqualTo("{'who':123}");
        then(fixture.operationName()).isEqualTo("greeting");
        then(greeting).isEqualTo("hi, foo");
    }

    @GraphQlClientApi
    interface PrimitiveLongApi {
        String greeting(long who);
    }

    @Test
    void shouldCallPrimitiveLongQuery() {
        fixture.returnsData("'greeting':'hi, foo'");
        PrimitiveLongApi api = fixture.build(PrimitiveLongApi.class);

        String greeting = api.greeting(123L);

        then(fixture.query()).isEqualTo("query greeting($who: BigInteger!) { greeting(who: $who) }");
        then(fixture.variables()).isEqualTo("{'who':123}");
        then(fixture.operationName()).isEqualTo("greeting");
        then(greeting).isEqualTo("hi, foo");
    }

    @GraphQlClientApi
    interface NonNullLongApi {
        String greeting(@NonNull Long who);
    }

    @Test
    void shouldCallNonNullLongQuery() {
        fixture.returnsData("'greeting':'hi, foo'");
        NonNullLongApi api = fixture.build(NonNullLongApi.class);

        String greeting = api.greeting(123L);

        then(fixture.query()).isEqualTo("query greeting($who: BigInteger!) { greeting(who: $who) }");
        then(fixture.variables()).isEqualTo("{'who':123}");
        then(fixture.operationName()).isEqualTo("greeting");
        then(greeting).isEqualTo("hi, foo");
    }
}
