package test.unit;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.assertj.core.api.BDDAssertions.then;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.eclipse.microprofile.graphql.NonNull;
import org.junit.jupiter.api.Test;

import io.smallrye.graphql.client.typesafe.api.GraphQlClientApi;
import io.smallrye.graphql.client.typesafe.api.GraphQlClientException;

public class NestedBehavior {
    private final GraphQlClientFixture fixture = new GraphQlClientFixture();

    @GraphQlClientApi
    interface StringSetApi {
        Set<String> greetings();
    }

    @Test
    public void shouldCallStringSetQuery() {
        fixture.returnsData("'greetings':['a','b']");
        StringSetApi api = fixture.builder().build(StringSetApi.class);

        Set<String> greetings = api.greetings();

        then(fixture.query()).isEqualTo("greetings");
        then(greetings).containsExactly("a", "b");
    }

    @Test
    public void shouldFailToAssignStringToSet() {
        fixture.returnsData("'greetings':'a'");
        StringSetApi api = fixture.builder().build(StringSetApi.class);

        GraphQlClientException thrown = catchThrowableOfType(api::greetings, GraphQlClientException.class);

        then(thrown).hasMessage(
            "invalid java.util.Set<java.lang.String> value for " + StringSetApi.class.getName() + "#greetings: \"a\"");
    }

    @Test
    public void shouldFailToAssignNumberToSet() {
        fixture.returnsData("'greetings':12345");
        StringSetApi api = fixture.builder().build(StringSetApi.class);

        GraphQlClientException thrown = catchThrowableOfType(api::greetings, GraphQlClientException.class);

        then(thrown).hasMessage(
            "invalid java.util.Set<java.lang.String> value for " + StringSetApi.class.getName() + "#greetings: 12345");
    }

    @Test
    public void shouldFailToAssignBooleanToSet() {
        fixture.returnsData("'greetings':true");
        StringSetApi api = fixture.builder().build(StringSetApi.class);

        GraphQlClientException thrown = catchThrowableOfType(api::greetings, GraphQlClientException.class);

        then(thrown).hasMessage(
            "invalid java.util.Set<java.lang.String> value for " + StringSetApi.class.getName() + "#greetings: true");
    }

    @Test
    public void shouldFailToAssignObjectToSet() {
        fixture.returnsData("'greetings':{'foo':'bar'}");
        StringSetApi api = fixture.builder().build(StringSetApi.class);

        GraphQlClientException thrown = catchThrowableOfType(api::greetings, GraphQlClientException.class);

        then(thrown).hasMessage("invalid java.util.Set<java.lang.String> value for " + StringSetApi.class.getName()
            + "#greetings: {\"foo\":\"bar\"}");
    }

    @GraphQlClientApi
    interface StringListApi {
        List<String> greetings();
    }

    @Test
    public void shouldCallStringListQuery() {
        fixture.returnsData("'greetings':['a','b']");
        StringListApi api = fixture.builder().build(StringListApi.class);

        List<String> greetings = api.greetings();

        then(fixture.query()).isEqualTo("greetings");
        then(greetings).containsExactly("a", "b");
    }

    @Test
    public void shouldCallStringListQueryWithNull() {
        fixture.returnsData("'greetings':['a',null]");
        StringListApi api = fixture.builder().build(StringListApi.class);

        List<String> greetings = api.greetings();

        then(fixture.query()).isEqualTo("greetings");
        then(greetings).containsExactly("a", null);
    }

    @GraphQlClientApi
    interface NonNullStringListApi {
        @SuppressWarnings("UnusedReturnValue")
        List<@NonNull String> greetings();
    }

    @Test
    public void shouldFailToCallNonNullStringListQuery() {
        fixture.returnsData("'greetings':['a',null]");
        NonNullStringListApi api = fixture.builder().build(NonNullStringListApi.class);

        GraphQlClientException thrown = catchThrowableOfType(api::greetings, GraphQlClientException.class);

        then(thrown).hasMessage(
            "invalid null java.lang.String value for " + NonNullStringListApi.class.getName() + "#greetings[1]");
    }

    @GraphQlClientApi
    interface StringArrayApi {
        String[] greetings();
    }

    @Test
    public void shouldCallStringArrayQuery() {
        fixture.returnsData("'greetings':['a','b']");
        StringArrayApi api = fixture.builder().build(StringArrayApi.class);

        String[] greetings = api.greetings();

        then(fixture.query()).isEqualTo("greetings");
        then(greetings).containsExactly("a", "b");
    }

    @GraphQlClientApi
    interface ObjectApi {
        Greeting greeting();
    }

    private static class Greeting {
        String text;
        int code;
        Boolean successful;

        @SuppressWarnings("unused") Greeting() {
        }

        Greeting(String text, int code, Boolean successful) {
            this.text = text;
            this.code = code;
            this.successful = successful;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            Greeting greeting = (Greeting) o;
            return code == greeting.code && text.equals(greeting.text) && successful == greeting.successful;
        }

        @Override
        public int hashCode() {
            return Objects.hash(text, code, successful);
        }
    }

    @Test
    public void shouldCallObjectQuery() {
        fixture.returnsData("'greeting':{'text':'foo','code':5, 'successful':true}");
        ObjectApi api = fixture.builder().build(ObjectApi.class);

        Greeting greeting = api.greeting();

        then(fixture.query()).isEqualTo("greeting {text code successful}");
        then(greeting).isEqualTo(new Greeting("foo", 5, true));
    }

    @Test
    public void shouldFailToAssignStringToObject() {
        fixture.returnsData("'greeting':'a'");
        ObjectApi api = fixture.builder().build(ObjectApi.class);

        GraphQlClientException thrown = catchThrowableOfType(api::greeting, GraphQlClientException.class);

        then(thrown).hasMessage(
            "invalid " + Greeting.class.getName() + " value for " + ObjectApi.class.getName() + "#greeting: \"a\"");
    }

    @Test
    public void shouldFailToAssignNumberToObject() {
        fixture.returnsData("'greeting':12.34");
        ObjectApi api = fixture.builder().build(ObjectApi.class);

        GraphQlClientException thrown = catchThrowableOfType(api::greeting, GraphQlClientException.class);

        then(thrown).hasMessage(
            "invalid " + Greeting.class.getName() + " value for " + ObjectApi.class.getName() + "#greeting: 12.34");
    }

    @Test
    public void shouldFailToAssignBooleanToObject() {
        fixture.returnsData("'greeting':false");
        ObjectApi api = fixture.builder().build(ObjectApi.class);

        GraphQlClientException thrown = catchThrowableOfType(api::greeting, GraphQlClientException.class);

        then(thrown).hasMessage(
            "invalid " + Greeting.class.getName() + " value for " + ObjectApi.class.getName() + "#greeting: false");
    }

    @GraphQlClientApi
    interface ObjectListApi {
        List<Greeting> greetings();
    }

    @Test
    public void shouldCallObjectListQuery() {
        fixture.returnsData("'greetings':["
            + "{'text':'a','code':1},"
            + "{'text':'b','code':2,'successful':true},"
            + "{'text':'c','code':3,'successful':false}]");
        ObjectListApi api = fixture.builder().build(ObjectListApi.class);

        List<Greeting> greeting = api.greetings();

        then(fixture.query()).isEqualTo("greetings {text code successful}");
        then(greeting).containsExactly(
            new Greeting("a", 1, null),
            new Greeting("b", 2, true),
            new Greeting("c", 3, false));
    }

    @Test
    public void shouldFailToAssignStringToObjectListQuery() {
        fixture.returnsData("'greetings':[{'text':'a','code':1},123456]");
        ObjectListApi api = fixture.builder().build(ObjectListApi.class);

        GraphQlClientException thrown = catchThrowableOfType(api::greetings, GraphQlClientException.class);

        then(thrown).hasMessage("invalid " + Greeting.class.getName() + " value for " + ObjectListApi.class.getName()
            + "#greetings[1]: 123456");
    }

    @Test
    public void shouldFailToAssignNumberToObjectListQuery() {
        fixture.returnsData("'greetings':[123,456.78]");
        ObjectListApi api = fixture.builder().build(ObjectListApi.class);

        GraphQlClientException thrown = catchThrowableOfType(api::greetings, GraphQlClientException.class);

        then(thrown).hasMessage(
            "invalid " + Greeting.class.getName() + " value for " + ObjectListApi.class.getName() + "#greetings[0]: 123");
    }

    @GraphQlClientApi
    interface StringContainerApi {
        StringContainer container();
    }

    private static class StringContainer {
        String greeting;
        int count;
    }

    @Test
    public void shouldCallNestedStringQuery() {
        fixture.returnsData("'container':{'greeting':'hi','count':5}");
        StringContainerApi api = fixture.builder().build(StringContainerApi.class);

        StringContainer container = api.container();

        then(fixture.query()).isEqualTo("container {greeting count}");
        then(container.greeting).isEqualTo("hi");
        then(container.count).isEqualTo(5);
    }

    @GraphQlClientApi
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
    public void shouldCallNestedObjectQuery() {
        fixture.returnsData("'container':{'greeting':{'text':'a','code':1},'count':3}");
        GreetingContainerApi api = fixture.builder().build(GreetingContainerApi.class);

        GreetingContainer container = api.container();

        then(fixture.query()).isEqualTo("container {greeting {text code successful} count}");
        then(container).isEqualTo(new GreetingContainer(
            new Greeting("a", 1, null), 3));
    }

    @GraphQlClientApi
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
    public void shouldCallNestedListQuery() {
        fixture.returnsData("'container':{'greetings':[" +
            "{'text':'a','code':1},{'text':'b','code':2}" +
            "],'count':3}");
        GreetingsContainerApi api = fixture.builder().build(GreetingsContainerApi.class);

        GreetingsContainer container = api.container();

        then(fixture.query()).isEqualTo("container {greetings {text code successful} count}");
        then(container).isEqualTo(new GreetingsContainer(
            asList(new Greeting("a", 1, null), new Greeting("b", 2, null)), 3));
    }

    @GraphQlClientApi
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
    public void shouldCallWrappedGreetingQuery() {
        fixture.returnsData("'container':{'greeting':{" +
            "'value':{'text':'a','code':1}}," +
            "'count':3}");
        WrappedGreetingApi api = fixture.builder().build(WrappedGreetingApi.class);

        WrappedGreetingContainer container = api.container();

        then(fixture.query()).isEqualTo("container {greeting {value {text code successful}} count}");
        then(container).isEqualTo(new WrappedGreetingContainer(
            new Wrapper<>(new Greeting("a", 1, null)), 3));
    }

    @GraphQlClientApi
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
    public void shouldCallWrappedByteQuery() {
        fixture.returnsData("'container':{'code':{'value':123},'count':3}");
        WrappedByteApi api = fixture.builder().build(WrappedByteApi.class);

        WrappedByteContainer container = api.container();

        then(fixture.query()).isEqualTo("container {code {value} count}");
        then(container).isEqualTo(new WrappedByteContainer(
            new Wrapper<>((byte) 123), 3));
    }

    @Test
    public void shouldFailToCallWrappedInvalidByteQuery() {
        fixture.returnsData("'container':{'code':{'value':1000},'count':3}");
        WrappedByteApi api = fixture.builder().build(WrappedByteApi.class);

        GraphQlClientException thrown = catchThrowableOfType(api::container, GraphQlClientException.class);

        then(thrown).hasMessage(
            "invalid java.lang.Byte value for " + WrappedByteApi.class.getName() + "#container.code.value: 1000");
    }

    @GraphQlClientApi
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
    public void shouldCallWrappedListByteQuery() {
        fixture.returnsData("'container':{'codes':[97,98,99],'count':3}");
        WrappedListByteApi api = fixture.builder().build(WrappedListByteApi.class);

        WrappedListByteContainer container = api.container();

        then(fixture.query()).isEqualTo("container {codes count}");
        then(container).isEqualTo(new WrappedListByteContainer(
            asList((byte) 'a', (byte) 'b', (byte) 'c'), 3));
    }

    @Test
    public void shouldFailToCallWrappedInvalidListByteQuery() {
        fixture.returnsData("'container':{'codes':[97,98,9999],'count':3}");
        WrappedListByteApi api = fixture.builder().build(WrappedListByteApi.class);

        GraphQlClientException thrown = catchThrowableOfType(api::container, GraphQlClientException.class);

        then(thrown).hasMessage(
            "invalid java.lang.Byte value for " + WrappedListByteApi.class.getName() + "#container.codes[2]: 9999");
    }

    @GraphQlClientApi
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
    public void shouldCallObjectQueryWithSpecialFields() {
        fixture.returnsData("'foo':{'text':'foo','code':5,'ignoreMe':true}");
        ClassWithTransientAndStaticFieldsApi api = fixture.builder().build(ClassWithTransientAndStaticFieldsApi.class);

        ClassWithTransientAndStaticFields foo = api.foo();

        then(fixture.query()).isEqualTo("foo {text code}");
        then(foo.text).isEqualTo("foo");
        then(foo.code).isEqualTo(5);
        then(foo.ignoreMe).isFalse();
    }

    @GraphQlClientApi
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
    public void shouldCallInheritedGreetingQuery() {
        fixture.returnsData("'call':{" +
            "'greeting':{'text':'a','code':1}," +
            "'count':3}");
        InheritedGreetingApi api = fixture.builder().build(InheritedGreetingApi.class);

        Sub sub = api.call();

        then(fixture.query()).isEqualTo("call {greeting {text code successful} count}");
        then(sub.greeting).isEqualTo(new Greeting("a", 1, null));
        then(sub.count).isEqualTo(3);
    }

    @GraphQlClientApi
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
    public void shouldCreateObjectPrivateDefaultConstructor() {
        fixture.returnsData("'call':{'foo':'a'}");
        ObjectPrivateDefaultConstructorApi api = fixture.builder().build(ObjectPrivateDefaultConstructorApi.class);

        ObjectPrivateDefaultConstructor call = api.call();

        then(fixture.query()).isEqualTo("call {foo}");
        then(call.foo).isEqualTo("a");
    }

    @GraphQlClientApi
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
    public void shouldFailToCreateObjectWithoutDefaultConstructor() {
        fixture.returnsData("'call':{'foo':'a','bar':'b'}");
        ObjectWithoutDefaultConstructorApi api = fixture.builder().build(ObjectWithoutDefaultConstructorApi.class);

        GraphQlClientException thrown = catchThrowableOfType(api::call, GraphQlClientException.class);

        then(thrown).hasMessage("can't create " + ObjectWithoutDefaultConstructor.class.getName() +
            " value for " + ObjectWithoutDefaultConstructorApi.class.getName() + "#call");
    }

    @GraphQlClientApi
    interface MissingNullableFieldApi {
        MissingNullableField call();
    }

    private static class MissingNullableField {
        @NonNull
        String foo;
        String bar;
    }

    @Test
    public void shouldCallWithMissingNullableField() {
        fixture.returnsData("'call':{'foo':'a'}");
        MissingNullableFieldApi api = fixture.builder().build(MissingNullableFieldApi.class);

        MissingNullableField result = api.call();

        then(fixture.query()).isEqualTo("call {foo bar}");
        then(result.foo).isEqualTo("a");
        then(result.bar).isNull();
    }

    @GraphQlClientApi
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
    public void shouldFailToSetMissingNonNullField() {
        fixture.returnsData("'call':{'foo':'a'}");
        MissingNonNullFieldApi api = fixture.builder().build(MissingNonNullFieldApi.class);

        GraphQlClientException thrown = catchThrowableOfType(api::call, GraphQlClientException.class);

        then(thrown).hasMessage("missing java.lang.String value for " + MissingNonNullFieldApi.class.getName() + "#call.bar");
    }

    @GraphQlClientApi
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
    public void shouldFailToSetMissingPrimitiveField() {
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

    @GraphQlClientApi
    private interface RecursiveApi {
        @SuppressWarnings({"UnusedReturnValue", "unused"})
        Hero member();
    }

    @Test
    public void shouldFailToCallApiWithRecursiveFields() {
        RecursiveApi api = fixture.builder().build(RecursiveApi.class);

        GraphQlClientException thrown = catchThrowableOfType(api::member, GraphQlClientException.class);

        then(thrown).hasMessage("field recursion found");
    }
}
