package tck.graphql.typesafe;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.assertj.core.api.BDDAssertions.then;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.eclipse.microprofile.graphql.NonNull;
import org.junit.jupiter.api.Test;

import io.smallrye.graphql.client.InvalidResponseException;
import io.smallrye.graphql.client.typesafe.api.GraphQLClientApi;

class NestedBehavior {
    private final TypesafeGraphQLClientFixture fixture = TypesafeGraphQLClientFixture.load();

    @GraphQLClientApi
    interface StringSetApi {
        Set<String> greetings();
    }

    @Test
    void shouldCallStringSetQuery() {
        fixture.returnsData("'greetings':['a','b']");
        StringSetApi api = fixture.build(StringSetApi.class);

        Set<String> greetings = api.greetings();

        then(fixture.query()).isEqualTo("query greetings { greetings }");
        then(greetings).containsExactly("a", "b");
    }

    @Test
    void shouldFailToAssignStringToSet() {
        fixture.returnsData("'greetings':'a'");
        StringSetApi api = fixture.build(StringSetApi.class);

        InvalidResponseException thrown = catchThrowableOfType(api::greetings, InvalidResponseException.class);

        then(thrown).hasMessage(
                "invalid java.util.Set<java.lang.String> value for " + StringSetApi.class.getName() + "#greetings: \"a\"");
    }

    @Test
    void shouldFailToAssignNumberToSet() {
        fixture.returnsData("'greetings':12345");
        StringSetApi api = fixture.build(StringSetApi.class);

        InvalidResponseException thrown = catchThrowableOfType(api::greetings, InvalidResponseException.class);

        then(thrown).hasMessage(
                "invalid java.util.Set<java.lang.String> value for " + StringSetApi.class.getName() + "#greetings: 12345");
    }

    @Test
    void shouldFailToAssignBooleanToSet() {
        fixture.returnsData("'greetings':true");
        StringSetApi api = fixture.build(StringSetApi.class);

        InvalidResponseException thrown = catchThrowableOfType(api::greetings, InvalidResponseException.class);

        then(thrown).hasMessage(
                "invalid java.util.Set<java.lang.String> value for " + StringSetApi.class.getName() + "#greetings: true");
    }

    @Test
    void shouldFailToAssignObjectToSet() {
        fixture.returnsData("'greetings':{'foo':'bar'}");
        StringSetApi api = fixture.build(StringSetApi.class);

        InvalidResponseException thrown = catchThrowableOfType(api::greetings, InvalidResponseException.class);

        then(thrown).hasMessage("invalid java.util.Set<java.lang.String> value for " + StringSetApi.class.getName()
                + "#greetings: {\"foo\":\"bar\"}");
    }

    @GraphQLClientApi
    interface StringCollectionApi {
        Collection<String> greetings();
    }

    @Test
    void shouldCallStringCollectionQuery() {
        fixture.returnsData("'greetings':['a','b']");
        StringCollectionApi api = fixture.build(StringCollectionApi.class);

        Collection<String> greetings = api.greetings();

        then(fixture.query()).isEqualTo("query greetings { greetings }");
        then(greetings).containsExactly("a", "b");
    }

    @GraphQLClientApi
    interface StringArrayListApi {
        ArrayList<String> greetings();
    }

    @Test
    void shouldCallStringArrayListQuery() {
        fixture.returnsData("'greetings':['a','b']");
        StringArrayListApi api = fixture.build(StringArrayListApi.class);

        ArrayList<String> greetings = api.greetings();

        then(fixture.query()).isEqualTo("query greetings { greetings }");
        then(greetings).containsExactly("a", "b");
    }

    @GraphQLClientApi
    interface StringListApi {
        List<String> greetings();
    }

    @Test
    void shouldCallStringListQuery() {
        fixture.returnsData("'greetings':['a','b']");
        StringListApi api = fixture.build(StringListApi.class);

        List<String> greetings = api.greetings();

        then(fixture.query()).isEqualTo("query greetings { greetings }");
        then(greetings).containsExactly("a", "b");
    }

    @Test
    void shouldCallStringListQueryWithNull() {
        fixture.returnsData("'greetings':['a',null]");
        StringListApi api = fixture.build(StringListApi.class);

        List<String> greetings = api.greetings();

        then(fixture.query()).isEqualTo("query greetings { greetings }");
        then(greetings).containsExactly("a", null);
    }

    @GraphQLClientApi
    interface NonNullStringListApi {
        @SuppressWarnings("UnusedReturnValue")
        List<@NonNull String> greetings();
    }

    @Test
    void shouldFailToCallNonNullStringListQuery() {
        fixture.returnsData("'greetings':['a',null]");
        NonNullStringListApi api = fixture.build(NonNullStringListApi.class);

        InvalidResponseException thrown = catchThrowableOfType(api::greetings, InvalidResponseException.class);

        then(thrown).hasMessage(
                "invalid null java.lang.String value for " + NonNullStringListApi.class.getName() + "#greetings[1]");
    }

    @GraphQLClientApi
    interface StringArrayApi {
        String[] greetings();
    }

    @Test
    void shouldCallStringArrayQuery() {
        fixture.returnsData("'greetings':['a','b']");
        StringArrayApi api = fixture.build(StringArrayApi.class);

        String[] greetings = api.greetings();

        then(fixture.query()).isEqualTo("query greetings { greetings }");
        then(greetings).containsExactly("a", "b");
    }

    @GraphQLClientApi
    interface ObjectApi {
        Greeting greeting();
    }

    private static class Greeting {
        String text;
        int code;
        Boolean successful;

        @SuppressWarnings("unused")
        Greeting() {
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
    void shouldCallObjectQuery() {
        fixture.returnsData("'greeting':{'text':'foo','code':5, 'successful':true}");
        ObjectApi api = fixture.build(ObjectApi.class);

        Greeting greeting = api.greeting();

        then(fixture.query()).isEqualTo("query greeting { greeting {text code successful} }");
        then(greeting).isEqualTo(new Greeting("foo", 5, true));
    }

    @Test
    void shouldFailToAssignStringToObject() {
        fixture.returnsData("'greeting':'a'");
        ObjectApi api = fixture.build(ObjectApi.class);

        InvalidResponseException thrown = catchThrowableOfType(api::greeting, InvalidResponseException.class);

        then(thrown).hasMessage(
                "invalid " + Greeting.class.getName() + " value for " + ObjectApi.class.getName() + "#greeting: \"a\"");
    }

    @Test
    void shouldFailToAssignNumberToObject() {
        fixture.returnsData("'greeting':12.34");
        ObjectApi api = fixture.build(ObjectApi.class);

        InvalidResponseException thrown = catchThrowableOfType(api::greeting, InvalidResponseException.class);

        then(thrown).hasMessage(
                "invalid " + Greeting.class.getName() + " value for " + ObjectApi.class.getName() + "#greeting: 12.34");
    }

    @Test
    void shouldFailToAssignBooleanToObject() {
        fixture.returnsData("'greeting':false");
        ObjectApi api = fixture.build(ObjectApi.class);

        InvalidResponseException thrown = catchThrowableOfType(api::greeting, InvalidResponseException.class);

        then(thrown).hasMessage(
                "invalid " + Greeting.class.getName() + " value for " + ObjectApi.class.getName() + "#greeting: false");
    }

    @GraphQLClientApi
    interface ObjectListApi {
        List<Greeting> greetings();
    }

    @Test
    void shouldCallObjectListQuery() {
        fixture.returnsData("'greetings':["
                + "{'text':'a','code':1},"
                + "{'text':'b','code':2,'successful':true},"
                + "{'text':'c','code':3,'successful':false}]");
        ObjectListApi api = fixture.build(ObjectListApi.class);

        List<Greeting> greeting = api.greetings();

        then(fixture.query()).isEqualTo("query greetings { greetings {text code successful} }");
        then(greeting).containsExactly(
                new Greeting("a", 1, null),
                new Greeting("b", 2, true),
                new Greeting("c", 3, false));
    }

    @Test
    void shouldFailToAssignStringToObjectListQuery() {
        fixture.returnsData("'greetings':[{'text':'a','code':1},123456]");
        ObjectListApi api = fixture.build(ObjectListApi.class);

        InvalidResponseException thrown = catchThrowableOfType(api::greetings, InvalidResponseException.class);

        then(thrown).hasMessage("invalid " + Greeting.class.getName() + " value for " + ObjectListApi.class.getName()
                + "#greetings[1]: 123456");
    }

    @Test
    void shouldFailToAssignNumberToObjectListQuery() {
        fixture.returnsData("'greetings':[123,456.78]");
        ObjectListApi api = fixture.build(ObjectListApi.class);

        InvalidResponseException thrown = catchThrowableOfType(api::greetings, InvalidResponseException.class);

        then(thrown).hasMessage(
                "invalid " + Greeting.class.getName() + " value for " + ObjectListApi.class.getName() + "#greetings[0]: 123");
    }

    @GraphQLClientApi
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
        StringContainerApi api = fixture.build(StringContainerApi.class);

        StringContainer container = api.container();

        then(fixture.query()).isEqualTo("query container { container {greeting count} }");
        then(container.greeting).isEqualTo("hi");
        then(container.count).isEqualTo(5);
    }

    @GraphQLClientApi
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
        GreetingContainerApi api = fixture.build(GreetingContainerApi.class);

        GreetingContainer container = api.container();

        then(fixture.query())
                .isEqualTo("query container { container {greeting {text code successful} count} }");
        then(container).isEqualTo(new GreetingContainer(
                new Greeting("a", 1, null), 3));
    }

    @GraphQLClientApi
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
        GreetingsContainerApi api = fixture.build(GreetingsContainerApi.class);

        GreetingsContainer container = api.container();

        then(fixture.query())
                .isEqualTo("query container { container {greetings {text code successful} count} }");
        then(container).isEqualTo(new GreetingsContainer(
                asList(new Greeting("a", 1, null), new Greeting("b", 2, null)), 3));
    }

    @GraphQLClientApi
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
        @SuppressWarnings({ "FieldCanBeLocal", "unused" })
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
        WrappedGreetingApi api = fixture.build(WrappedGreetingApi.class);

        WrappedGreetingContainer container = api.container();

        then(fixture.query())
                .isEqualTo("query container { container {greeting {value {text code successful}} count} }");
        then(container).isEqualTo(new WrappedGreetingContainer(
                new Wrapper<>(new Greeting("a", 1, null)), 3));
    }

    @GraphQLClientApi
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
        WrappedByteApi api = fixture.build(WrappedByteApi.class);

        WrappedByteContainer container = api.container();

        then(fixture.query()).isEqualTo("query container { container {code {value} count} }");
        then(container).isEqualTo(new WrappedByteContainer(
                new Wrapper<>((byte) 123), 3));
    }

    @Test
    void shouldFailToCallWrappedInvalidByteQuery() {
        fixture.returnsData("'container':{'code':{'value':1000},'count':3}");
        WrappedByteApi api = fixture.build(WrappedByteApi.class);

        InvalidResponseException thrown = catchThrowableOfType(api::container, InvalidResponseException.class);

        then(thrown).hasMessage(
                "invalid java.lang.Byte value for " + WrappedByteApi.class.getName() + "#container.code.value: 1000");
    }

    @GraphQLClientApi
    interface WrappedListByteApi {
        WrappedListByteContainer container();
    }

    private static class WrappedListByteContainer {
        List<Byte> codes;
        int count;

        @SuppressWarnings("unused")
        WrappedListByteContainer() {
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
        WrappedListByteApi api = fixture.build(WrappedListByteApi.class);

        WrappedListByteContainer container = api.container();

        then(fixture.query()).isEqualTo("query container { container {codes count} }");
        then(container).isEqualTo(new WrappedListByteContainer(
                asList((byte) 'a', (byte) 'b', (byte) 'c'), 3));
    }

    @Test
    void shouldFailToCallWrappedInvalidListByteQuery() {
        fixture.returnsData("'container':{'codes':[97,98,9999],'count':3}");
        WrappedListByteApi api = fixture.build(WrappedListByteApi.class);

        InvalidResponseException thrown = catchThrowableOfType(api::container, InvalidResponseException.class);

        then(thrown).hasMessage(
                "invalid java.lang.Byte value for " + WrappedListByteApi.class.getName() + "#container.codes[2]: 9999");
    }

    @GraphQLClientApi
    interface ClassWithTransientAndStaticFieldsApi {
        ClassWithTransientAndStaticFields foo();
    }

    @SuppressWarnings({ "unused" })
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
        ClassWithTransientAndStaticFieldsApi api = fixture.build(ClassWithTransientAndStaticFieldsApi.class);

        ClassWithTransientAndStaticFields foo = api.foo();

        then(fixture.query()).isEqualTo("query foo { foo {text code} }");
        then(foo.text).isEqualTo("foo");
        then(foo.code).isEqualTo(5);
        then(foo.ignoreMe).isFalse();
    }

    @GraphQLClientApi
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
        InheritedGreetingApi api = fixture.build(InheritedGreetingApi.class);

        Sub sub = api.call();

        then(fixture.query())
                .isEqualTo("query call { call {greeting {text code successful} count} }");
        then(sub.greeting).isEqualTo(new Greeting("a", 1, null));
        then(sub.count).isEqualTo(3);
    }

    @GraphQLClientApi
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
        ObjectPrivateDefaultConstructorApi api = fixture.build(ObjectPrivateDefaultConstructorApi.class);

        ObjectPrivateDefaultConstructor call = api.call();

        then(fixture.query()).isEqualTo("query call { call {foo} }");
        then(call.foo).isEqualTo("a");
    }

    @GraphQLClientApi
    interface ObjectWithoutDefaultConstructorApi {
        @SuppressWarnings("UnusedReturnValue")
        ObjectWithoutDefaultConstructor call();
    }

    private static class ObjectWithoutDefaultConstructor {
        @SuppressWarnings({ "FieldCanBeLocal", "unused" })
        private final String foo;
        @SuppressWarnings({ "FieldCanBeLocal", "unused" })
        private final String bar;

        public ObjectWithoutDefaultConstructor(String foo, String bar) {
            this.foo = foo;
            this.bar = bar;
        }
    }

    @Test
    void shouldFailToCreateObjectWithoutDefaultConstructor() {
        fixture.returnsData("'call':{'foo':'a','bar':'b'}");
        ObjectWithoutDefaultConstructorApi api = fixture.build(ObjectWithoutDefaultConstructorApi.class);

        Exception thrown = catchThrowableOfType(api::call, Exception.class);

        then(thrown).hasMessageContaining("Cannot instantiate " + ObjectWithoutDefaultConstructor.class.getName() +
                " value for " + ObjectWithoutDefaultConstructorApi.class.getName() + "#call");
    }

    @GraphQLClientApi
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
        MissingNullableFieldApi api = fixture.build(MissingNullableFieldApi.class);

        MissingNullableField result = api.call();

        then(fixture.query()).isEqualTo("query call { call {foo bar} }");
        then(result.foo).isEqualTo("a");
        then(result.bar).isNull();
    }

    @GraphQLClientApi
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
        MissingNonNullFieldApi api = fixture.build(MissingNonNullFieldApi.class);

        InvalidResponseException thrown = catchThrowableOfType(api::call, InvalidResponseException.class);

        then(thrown).hasMessage("missing java.lang.String value for " + MissingNonNullFieldApi.class.getName() + "#call.bar");
    }

    @GraphQLClientApi
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
        MissingPrimitiveFieldApi api = fixture.build(MissingPrimitiveFieldApi.class);

        InvalidResponseException thrown = catchThrowableOfType(api::call, InvalidResponseException.class);

        then(thrown).hasMessage("missing boolean value for " + MissingPrimitiveFieldApi.class.getName() + "#call.bar");
    }
}
