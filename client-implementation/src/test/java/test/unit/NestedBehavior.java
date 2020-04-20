package test.unit;

import io.smallrye.graphql.client.typesafe.api.GraphQlClientException;
import org.eclipse.microprofile.graphql.NonNull;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.assertj.core.api.BDDAssertions.then;

class NestedBehavior {
    private final GraphQlClientFixture fixture = new GraphQlClientFixture();

    interface StringSetApi {
        Set<String> greetings();
    }

    @Test
    void shouldCallStringSetQuery() {
        fixture.returnsData("'greetings':['a','b']");
        StringSetApi api = fixture.builder().build(StringSetApi.class);

        Set<String> greetings = api.greetings();

        then(fixture.query()).isEqualTo("greetings");
        then(greetings).containsExactly("a", "b");
    }

    @Test
    void shouldFailToAssignStringToSet() {
        fixture.returnsData("'greetings':'a'");
        StringSetApi api = fixture.builder().build(StringSetApi.class);

        GraphQlClientException thrown = catchThrowableOfType(api::greetings, GraphQlClientException.class);

        then(thrown).hasMessage(
            "invalid java.util.Set<java.lang.String> value for " + StringSetApi.class.getName() + "#greetings: \"a\"");
    }

    @Test
    void shouldFailToAssignNumberToSet() {
        fixture.returnsData("'greetings':12345");
        StringSetApi api = fixture.builder().build(StringSetApi.class);

        GraphQlClientException thrown = catchThrowableOfType(api::greetings, GraphQlClientException.class);

        then(thrown).hasMessage(
            "invalid java.util.Set<java.lang.String> value for " + StringSetApi.class.getName() + "#greetings: 12345");
    }

    @Test
    void shouldFailToAssignBooleanToSet() {
        fixture.returnsData("'greetings':true");
        StringSetApi api = fixture.builder().build(StringSetApi.class);

        GraphQlClientException thrown = catchThrowableOfType(api::greetings, GraphQlClientException.class);

        then(thrown).hasMessage(
            "invalid java.util.Set<java.lang.String> value for " + StringSetApi.class.getName() + "#greetings: true");
    }

    @Test
    void shouldFailToAssignObjectToSet() {
        fixture.returnsData("'greetings':{'foo':'bar'}");
        StringSetApi api = fixture.builder().build(StringSetApi.class);

        GraphQlClientException thrown = catchThrowableOfType(api::greetings, GraphQlClientException.class);

        then(thrown).hasMessage("invalid java.util.Set<java.lang.String> value for " + StringSetApi.class.getName()
            + "#greetings: {\"foo\":\"bar\"}");
    }

    interface StringListApi {
        List<String> greetings();
    }

    @Test
    void shouldCallStringListQuery() {
        fixture.returnsData("'greetings':['a','b']");
        StringListApi api = fixture.builder().build(StringListApi.class);

        List<String> greetings = api.greetings();

        then(fixture.query()).isEqualTo("greetings");
        then(greetings).containsExactly("a", "b");
    }

    @Test
    void shouldCallStringListQueryWithNull() {
        fixture.returnsData("'greetings':['a',null]");
        StringListApi api = fixture.builder().build(StringListApi.class);

        List<String> greetings = api.greetings();

        then(fixture.query()).isEqualTo("greetings");
        then(greetings).containsExactly("a", null);
    }

    interface NonNullStringListApi {
        @SuppressWarnings("UnusedReturnValue")
        List<@NonNull String> greetings();
    }

    @Test
    void shouldFailToCallNonNullStringListQuery() {
        fixture.returnsData("'greetings':['a',null]");
        NonNullStringListApi api = fixture.builder().build(NonNullStringListApi.class);

        GraphQlClientException thrown = catchThrowableOfType(api::greetings, GraphQlClientException.class);

        then(thrown).hasMessage(
            "invalid null java.lang.String value for " + NonNullStringListApi.class.getName() + "#greetings[1]");
    }

    interface StringArrayApi {
        String[] greetings();
    }

    @Test
    void shouldCallStringArrayQuery() {
        fixture.returnsData("'greetings':['a','b']");
        StringArrayApi api = fixture.builder().build(StringArrayApi.class);

        String[] greetings = api.greetings();

        then(fixture.query()).isEqualTo("greetings");
        then(greetings).containsExactly("a", "b");
    }

    interface ObjectApi {
        Greeting greeting();
    }

    private static class Greeting {
        String text;
        int code;

        @SuppressWarnings("unused") Greeting() {
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
    void shouldCallObjectQuery() {
        fixture.returnsData("'greeting':{'text':'foo','code':5}");
        ObjectApi api = fixture.builder().build(ObjectApi.class);

        Greeting greeting = api.greeting();

        then(fixture.query()).isEqualTo("greeting {text code}");
        then(greeting).isEqualTo(new Greeting("foo", 5));
    }

    @Test
    void shouldFailToAssignStringToObject() {
        fixture.returnsData("'greeting':'a'");
        ObjectApi api = fixture.builder().build(ObjectApi.class);

        GraphQlClientException thrown = catchThrowableOfType(api::greeting, GraphQlClientException.class);

        then(thrown).hasMessage(
            "invalid " + Greeting.class.getName() + " value for " + ObjectApi.class.getName() + "#greeting: \"a\"");
    }

    @Test
    void shouldFailToAssignNumberToObject() {
        fixture.returnsData("'greeting':12.34");
        ObjectApi api = fixture.builder().build(ObjectApi.class);

        GraphQlClientException thrown = catchThrowableOfType(api::greeting, GraphQlClientException.class);

        then(thrown).hasMessage(
            "invalid " + Greeting.class.getName() + " value for " + ObjectApi.class.getName() + "#greeting: 12.34");
    }

    @Test
    void shouldFailToAssignBooleanToObject() {
        fixture.returnsData("'greeting':false");
        ObjectApi api = fixture.builder().build(ObjectApi.class);

        GraphQlClientException thrown = catchThrowableOfType(api::greeting, GraphQlClientException.class);

        then(thrown).hasMessage(
            "invalid " + Greeting.class.getName() + " value for " + ObjectApi.class.getName() + "#greeting: false");
    }

    interface ObjectListApi {
        List<Greeting> greetings();
    }

    @Test
    void shouldCallObjectListQuery() {
        fixture.returnsData("'greetings':[{'text':'a','code':1},{'text':'b','code':2}]");
        ObjectListApi api = fixture.builder().build(ObjectListApi.class);

        List<Greeting> greeting = api.greetings();

        then(fixture.query()).isEqualTo("greetings {text code}");
        then(greeting).containsExactly(
            new Greeting("a", 1),
            new Greeting("b", 2));
    }

    @Test
    void shouldFailToAssignStringToObjectListQuery() {
        fixture.returnsData("'greetings':[{'text':'a','code':1},123456]");
        ObjectListApi api = fixture.builder().build(ObjectListApi.class);

        GraphQlClientException thrown = catchThrowableOfType(api::greetings, GraphQlClientException.class);

        then(thrown).hasMessage("invalid " + Greeting.class.getName() + " value for " + ObjectListApi.class.getName()
            + "#greetings[1]: 123456");
    }

    @Test
    void shouldFailToAssignNumberToObjectListQuery() {
        fixture.returnsData("'greetings':[123,456.78]");
        ObjectListApi api = fixture.builder().build(ObjectListApi.class);

        GraphQlClientException thrown = catchThrowableOfType(api::greetings, GraphQlClientException.class);

        then(thrown).hasMessage(
            "invalid " + Greeting.class.getName() + " value for " + ObjectListApi.class.getName() + "#greetings[0]: 123");
    }

    interface StringContainerApi {
        StringContainer container();
    }

    private static class StringContainer {
        String greeting;
        int count;
    }

    @Test
    void shouldCallNestedStringQuery() {
        fixture.returnsData("'container':{'greeting':'hi','count':5}");
        StringContainerApi api = fixture.builder().build(StringContainerApi.class);

        StringContainer container = api.container();

        then(fixture.query()).isEqualTo("container {greeting count}");
        then(container.greeting).isEqualTo("hi");
        then(container.count).isEqualTo(5);
    }

    interface GreetingContainerApi {
        GreetingContainer container();
    }

    private static class GreetingContainer {
        Greeting greeting;
        int count;

        @SuppressWarnings("unused")
        public GreetingContainer() {
        }

        public GreetingContainer(Greeting greeting, int count) {
            this.greeting = greeting;
            this.count = count;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            GreetingContainer that = (GreetingContainer) o;
            return count == that.count && greeting.equals(that.greeting);
        }

        @Override
        public int hashCode() {
            return Objects.hash(greeting, count);
        }
    }

    @Test
    void shouldCallNestedObjectQuery() {
        fixture.returnsData("'container':{'greeting':{'text':'a','code':1},'count':3}");
        GreetingContainerApi api = fixture.builder().build(GreetingContainerApi.class);

        GreetingContainer container = api.container();

        then(fixture.query()).isEqualTo("container {greeting {text code} count}");
        then(container).isEqualTo(new GreetingContainer(
            new Greeting("a", 1), 3));
    }

    interface GreetingsContainerApi {
        GreetingsContainer container();
    }

    private static class GreetingsContainer {
        List<Greeting> greetings;
        int count;

        @SuppressWarnings("unused")
        public GreetingsContainer() {
        }

        public GreetingsContainer(List<Greeting> greetings, int count) {
            this.greetings = greetings;
            this.count = count;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            GreetingsContainer that = (GreetingsContainer) o;
            return count == that.count && greetings.equals(that.greetings);
        }

        @Override
        public int hashCode() {
            return Objects.hash(greetings, count);
        }
    }

    @Test
    void shouldCallNestedListQuery() {
        fixture.returnsData("'container':{'greetings':[" +
            "{'text':'a','code':1},{'text':'b','code':2}" +
            "],'count':3}");
        GreetingsContainerApi api = fixture.builder().build(GreetingsContainerApi.class);

        GreetingsContainer container = api.container();

        then(fixture.query()).isEqualTo("container {greetings {text code} count}");
        then(container).isEqualTo(new GreetingsContainer(
            asList(new Greeting("a", 1), new Greeting("b", 2)), 3));
    }

    interface WrappedGreetingApi {
        WrappedGreetingContainer container();
    }

    private static class WrappedGreetingContainer {
        Wrapper<Greeting> greeting;
        int count;

        @SuppressWarnings("unused")
        public WrappedGreetingContainer() {
        }

        public WrappedGreetingContainer(Wrapper<Greeting> greeting, int count) {
            this.greeting = greeting;
            this.count = count;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            WrappedGreetingContainer that = (WrappedGreetingContainer) o;
            return count == that.count && greeting.equals(that.greeting);
        }

        @Override
        public int hashCode() {
            return Objects.hash(greeting, count);
        }
    }

    private static class Wrapper<T> {
        @SuppressWarnings({"FieldCanBeLocal", "unused"})
        private T value;

        @SuppressWarnings("unused")
        public Wrapper() {
        }

        public Wrapper(T value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            Wrapper<?> wrapper = (Wrapper<?>) o;
            return value.equals(wrapper.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }
    }

    @Test
    void shouldCallWrappedGreetingQuery() {
        fixture.returnsData("'container':{'greeting':{" +
            "'value':{'text':'a','code':1}}," +
            "'count':3}");
        WrappedGreetingApi api = fixture.builder().build(WrappedGreetingApi.class);

        WrappedGreetingContainer container = api.container();

        then(fixture.query()).isEqualTo("container {greeting {value {text code}} count}");
        then(container).isEqualTo(new WrappedGreetingContainer(
            new Wrapper<>(new Greeting("a", 1)), 3));
    }

    interface WrappedByteApi {
        WrappedByteContainer container();
    }

    private static class WrappedByteContainer {
        Wrapper<Byte> code;
        int count;

        @SuppressWarnings("unused")
        public WrappedByteContainer() {
        }

        public WrappedByteContainer(Wrapper<Byte> code, int count) {
            this.code = code;
            this.count = count;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            WrappedByteContainer that = (WrappedByteContainer) o;
            return count == that.count && code.equals(that.code);
        }

        @Override
        public int hashCode() {
            return Objects.hash(code, count);
        }
    }

    @Test
    void shouldCallWrappedByteQuery() {
        fixture.returnsData("'container':{'code':{'value':123},'count':3}");
        WrappedByteApi api = fixture.builder().build(WrappedByteApi.class);

        WrappedByteContainer container = api.container();

        then(fixture.query()).isEqualTo("container {code {value} count}");
        then(container).isEqualTo(new WrappedByteContainer(
            new Wrapper<>((byte) 123), 3));
    }

    @Test
    void shouldFailToCallWrappedInvalidByteQuery() {
        fixture.returnsData("'container':{'code':{'value':1000},'count':3}");
        WrappedByteApi api = fixture.builder().build(WrappedByteApi.class);

        GraphQlClientException thrown = catchThrowableOfType(api::container, GraphQlClientException.class);

        then(thrown).hasMessage(
            "invalid java.lang.Byte value for " + WrappedByteApi.class.getName() + "#container.code.value: 1000");
    }

    interface WrappedListByteApi {
        WrappedListByteContainer container();
    }

    private static class WrappedListByteContainer {
        List<Byte> codes;
        int count;

        @SuppressWarnings("unused") WrappedListByteContainer() {
        }

        WrappedListByteContainer(List<Byte> codes, int count) {
            this.codes = codes;
            this.count = count;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            WrappedListByteContainer that = (WrappedListByteContainer) o;
            return count == that.count && codes.equals(that.codes);
        }

        @Override
        public int hashCode() {
            return Objects.hash(codes, count);
        }
    }

    @Test
    void shouldCallWrappedListByteQuery() {
        fixture.returnsData("'container':{'codes':[97,98,99],'count':3}");
        WrappedListByteApi api = fixture.builder().build(WrappedListByteApi.class);

        WrappedListByteContainer container = api.container();

        then(fixture.query()).isEqualTo("container {codes count}");
        then(container).isEqualTo(new WrappedListByteContainer(
            asList((byte) 'a', (byte) 'b', (byte) 'c'), 3));
    }

    @Test
    void shouldFailToCallWrappedInvalidListByteQuery() {
        fixture.returnsData("'container':{'codes':[97,98,9999],'count':3}");
        WrappedListByteApi api = fixture.builder().build(WrappedListByteApi.class);

        GraphQlClientException thrown = catchThrowableOfType(api::container, GraphQlClientException.class);

        then(thrown).hasMessage(
            "invalid java.lang.Byte value for " + WrappedListByteApi.class.getName() + "#container.codes[2]: 9999");
    }

    interface ClassWithTransientAndStaticFieldsApi {
        ClassWithTransientAndStaticFields foo();
    }

    @SuppressWarnings({"unused"})
    private static class ClassWithTransientAndStaticFields {
        public static final String TO_BE_IGNORED = "foo";
        private static final String ALSO_TO_BE_IGNORED = "bar";
        private transient boolean ignoreMe;

        String text;
        int code;
    }

    @Test
    void shouldCallObjectQueryWithSpecialFields() {
        fixture.returnsData("'foo':{'text':'foo','code':5,'ignoreMe':true}");
        ClassWithTransientAndStaticFieldsApi api = fixture.builder().build(ClassWithTransientAndStaticFieldsApi.class);

        ClassWithTransientAndStaticFields foo = api.foo();

        then(fixture.query()).isEqualTo("foo {text code}");
        then(foo.text).isEqualTo("foo");
        then(foo.code).isEqualTo(5);
        then(foo.ignoreMe).isFalse();
    }

    interface InheritedGreetingApi {
        Sub call();
    }

    private static class Sub extends Super {
        int count;
    }

    private static class Super {
        Greeting greeting;
    }

    @Test
    void shouldCallInheritedGreetingQuery() {
        fixture.returnsData("'call':{" +
            "'greeting':{'text':'a','code':1}," +
            "'count':3}");
        InheritedGreetingApi api = fixture.builder().build(InheritedGreetingApi.class);

        Sub sub = api.call();

        then(fixture.query()).isEqualTo("call {greeting {text code} count}");
        then(sub.greeting).isEqualTo(new Greeting("a", 1));
        then(sub.count).isEqualTo(3);
    }

    interface ObjectPrivateDefaultConstructorApi {
        ObjectPrivateDefaultConstructor call();
    }

    private static class ObjectPrivateDefaultConstructor {
        private final String foo;

        private ObjectPrivateDefaultConstructor() {
            this.foo = null;
        }
    }

    @Test
    void shouldCreateObjectPrivateDefaultConstructor() {
        fixture.returnsData("'call':{'foo':'a'}");
        ObjectPrivateDefaultConstructorApi api = fixture.builder().build(ObjectPrivateDefaultConstructorApi.class);

        ObjectPrivateDefaultConstructor call = api.call();

        then(fixture.query()).isEqualTo("call {foo}");
        then(call.foo).isEqualTo("a");
    }

    interface ObjectWithoutDefaultConstructorApi {
        @SuppressWarnings("UnusedReturnValue")
        ObjectWithoutDefaultConstructor call();
    }

    private static class ObjectWithoutDefaultConstructor {
        @SuppressWarnings({"FieldCanBeLocal", "unused"})
        private final String foo;
        @SuppressWarnings({"FieldCanBeLocal", "unused"})
        private final String bar;

        public ObjectWithoutDefaultConstructor(String foo, String bar) {
            this.foo = foo;
            this.bar = bar;
        }
    }

    @Test
    void shouldFailToCreateObjectWithoutDefaultConstructor() {
        fixture.returnsData("'call':{'foo':'a','bar':'b'}");
        ObjectWithoutDefaultConstructorApi api = fixture.builder().build(ObjectWithoutDefaultConstructorApi.class);

        GraphQlClientException thrown = catchThrowableOfType(api::call, GraphQlClientException.class);

        then(thrown).hasMessage("can't create " + ObjectWithoutDefaultConstructor.class.getName() +
            " value for " + ObjectWithoutDefaultConstructorApi.class.getName() + "#call");
    }

    interface MissingNullableFieldApi {
        MissingNullableField call();
    }

    private static class MissingNullableField {
        @NonNull
        String foo;
        String bar;
    }

    @Test
    void shouldCallWithMissingNullableField() {
        fixture.returnsData("'call':{'foo':'a'}");
        MissingNullableFieldApi api = fixture.builder().build(MissingNullableFieldApi.class);

        MissingNullableField result = api.call();

        then(fixture.query()).isEqualTo("call {foo bar}");
        then(result.foo).isEqualTo("a");
        then(result.bar).isNull();
    }

    interface MissingNonNullFieldApi {
        @SuppressWarnings("UnusedReturnValue")
        MissingNonNullField call();
    }

    private static class MissingNonNullField {
        @SuppressWarnings("unused")
        String foo;

        @NonNull
        @SuppressWarnings("unused")
        String bar;
    }

    @Test
    void shouldFailToSetMissingNonNullField() {
        fixture.returnsData("'call':{'foo':'a'}");
        MissingNonNullFieldApi api = fixture.builder().build(MissingNonNullFieldApi.class);

        GraphQlClientException thrown = catchThrowableOfType(api::call, GraphQlClientException.class);

        then(thrown).hasMessage("missing java.lang.String value for " + MissingNonNullFieldApi.class.getName() + "#call.bar");
    }

    interface MissingPrimitiveFieldApi {
        @SuppressWarnings("UnusedReturnValue")
        MissingPrimitiveField call();
    }

    private static class MissingPrimitiveField {
        @SuppressWarnings("unused")
        String foo;
        @SuppressWarnings("unused")
        boolean bar;
    }

    @Test
    void shouldFailToSetMissingPrimitiveField() {
        fixture.returnsData("'call':{'foo':'a'}");
        MissingPrimitiveFieldApi api = fixture.builder().build(MissingPrimitiveFieldApi.class);

        GraphQlClientException thrown = catchThrowableOfType(api::call, GraphQlClientException.class);

        then(thrown).hasMessage("missing boolean value for " + MissingPrimitiveFieldApi.class.getName() + "#call.bar");
    }


    @SuppressWarnings("unused")
    private static class Hero {
        String name;
        List<Team> teams;
    }

    @SuppressWarnings("unused")
    private static class Team {
        String name;
        List<Hero> heroes;
    }

    private interface RecursiveApi {
        @SuppressWarnings({"UnusedReturnValue", "unused"}) Hero member();
    }

    @Test
    void shouldFailToCallApiWithRecursiveFields() {
        RecursiveApi api = fixture.builder().build(RecursiveApi.class);

        GraphQlClientException thrown = catchThrowableOfType(api::member, GraphQlClientException.class);

        then(thrown).hasMessage("field recursion found");
    }
}
