package test.unit;

import static java.util.Arrays.asList;
import static org.assertj.core.api.BDDAssertions.then;

import java.time.LocalDateTime;
import java.util.List;
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

        then(fixture.mutation()).isEqualTo("createSome(thing: $thing)");
        then(fixture.variables()).isEqualTo("{'thing':'input'}");
        then(greeting).isEqualTo("output");
    }

    @Test
    public void shouldCallNullStringMutation() {
        fixture.returnsData("'createSome':'output'");
        StringMutationApi api = fixture.builder().build(StringMutationApi.class);

        String greeting = api.createSome(null);

        then(fixture.mutation()).isEqualTo("createSome(thing: $thing)");
        then(fixture.variables()).isEqualTo("{'thing':null}");
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

        then(fixture.mutation()).isEqualTo("say(greet: $greet) {text count}");
        then(fixture.variables()).isEqualTo("{'greet':{'text':'hi','count':5}}");
        then(greeting).isEqualTo(new Greeting("ho", 3));
    }

    @Test
    public void shouldCallGreetingMutationWithNullValue() {
        fixture.returnsData("'say':{'text':'ho','count':3}");
        GreetingMutationApi api = fixture.builder().build(GreetingMutationApi.class);

        Greeting greeting = api.say(new Greeting(null, 5));

        then(fixture.mutation()).isEqualTo("say(greet: $greet) {text count}");
        then(fixture.variables()).isEqualTo("{'greet':{'text':null,'count':5}}");
        then(greeting).isEqualTo(new Greeting("ho", 3));
    }

    @GraphQlClientApi
    interface GreetingListMutationApi {
        @Mutation
        Greeting say(List<Greeting> greets);
    }

    @Test
    public void shouldCallMutationWithListWithNullValue() {
        fixture.returnsData("'say':{'text':'ho','count':3}");
        GreetingListMutationApi api = fixture.builder().build(GreetingListMutationApi.class);

        Greeting greeting = api.say(asList(
                new Greeting("one", 5),
                null,
                new Greeting("three", 5)));

        then(fixture.mutation()).isEqualTo("say(greets: $greets) {text count}");
        then(fixture.variables()).isEqualTo("{'greets':[" +
                "{'text':'one','count':5}," +
                "null," +
                "{'text':'three','count':5}" +
                "]}");
        then(greeting).isEqualTo(new Greeting("ho", 3));
    }

    private static class GreetingContainer {
        Greeting greeting;
        LocalDateTime when;

        @SuppressWarnings("unused") GreetingContainer() {
        }

        GreetingContainer(Greeting greeting, LocalDateTime when) {
            this.greeting = greeting;
            this.when = when;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            GreetingContainer that = (GreetingContainer) o;
            return Objects.equals(greeting, that.greeting) &&
                    Objects.equals(when, that.when);
        }

        @Override
        public int hashCode() {
            return Objects.hash(greeting, when);
        }
    }

    @GraphQlClientApi
    interface NestedGreetingMutationApi {
        @Mutation
        Greeting say(GreetingContainer greeting);
    }

    @Test
    public void shouldCallMutationWithNestedValue() {
        fixture.returnsData("'say':{'text':'ho','count':3}");
        NestedGreetingMutationApi api = fixture.builder().build(NestedGreetingMutationApi.class);
        LocalDateTime now = LocalDateTime.now();

        Greeting greeting = api.say(new GreetingContainer(new Greeting("one", 5), now));

        then(fixture.mutation()).isEqualTo("say(greeting: $greeting) {text count}");
        then(fixture.variables()).isEqualTo("{'greeting':{" +
                "'greeting':{'text':'one','count':5}," +
                "'when':'" + now + "'}}");
        then(greeting).isEqualTo(new Greeting("ho", 3));
    }

    @Test
    public void shouldCallMutationWithNestedNullValue() {
        fixture.returnsData("'say':{'text':'ho','count':3}");
        NestedGreetingMutationApi api = fixture.builder().build(NestedGreetingMutationApi.class);

        Greeting greeting = api.say(new GreetingContainer(new Greeting(null, 5), null));

        then(fixture.mutation()).isEqualTo("say(greeting: $greeting) {text count}");
        then(fixture.variables()).isEqualTo("{'greeting':{'greeting':{'text':null,'count':5},'when':null}}");
        then(greeting).isEqualTo(new Greeting("ho", 3));
    }

    @SuppressWarnings("unused")
    private enum SomeEnum {
        ONE,
        TWO,
        THREE
    }

    private static class GreetingEnum {
        String text;
        SomeEnum someEnum;

        @SuppressWarnings("unused")
        public GreetingEnum() {
        }

        private GreetingEnum(String text, SomeEnum someEnum) {
            this.text = text;
            this.someEnum = someEnum;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            GreetingEnum that = (GreetingEnum) o;
            return Objects.equals(text, that.text) &&
                    someEnum == that.someEnum;
        }

        @Override
        public int hashCode() {
            return Objects.hash(text, someEnum);
        }
    }

    @GraphQlClientApi
    interface MutationWithEnumApi {
        @Mutation
        Greeting say(GreetingEnum greeting);
    }

    @Test
    public void shouldCallMutationWithEnum() {
        fixture.returnsData("'say':{'text':'ho','count':3}");
        MutationWithEnumApi api = fixture.builder().build(MutationWithEnumApi.class);

        Greeting greeting = api.say(new GreetingEnum("one", SomeEnum.ONE));

        then(fixture.mutation()).isEqualTo("say(greeting: $greeting) {text count}");
        then(fixture.variables()).isEqualTo("{'greeting':{'text':'one','someEnum':'ONE'}}");
        then(greeting).isEqualTo(new Greeting("ho", 3));
    }

    private static class PrimitiveTypesClass {
        boolean b = true;
        char c = 'a';
        byte y = 0x7;
        short s = 0xff;
        int i = 123456;
        long l = 987654321L;
        float f = 1.0f;
        double d = 56.78d;

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            PrimitiveTypesClass that = (PrimitiveTypesClass) o;
            return this.b == that.b
                    && this.c == that.c
                    && this.y == that.y
                    && this.s == that.s
                    && this.i == that.i
                    && this.l == that.l
                    && this.f == that.f
                    && this.d == that.d;
        }

        @Override
        public int hashCode() {
            return Objects.hash(b, c, y, s, i, l, f, d);
        }
    }

    @GraphQlClientApi
    interface MutationWithPrimitivesApi {
        @Mutation
        String run(PrimitiveTypesClass primitives);
    }

    @Test
    public void shouldCallMutationWithPrimitives() {
        fixture.returnsData("'run':'okay'");
        MutationWithPrimitivesApi api = fixture.builder().build(MutationWithPrimitivesApi.class);

        String result = api.run(new PrimitiveTypesClass());

        then(fixture.mutation()).isEqualTo("run(primitives: $primitives)");
        then(fixture.variables()).isEqualTo("{'primitives':{'b':true,'c':'a','y':7,'s':255,'i':123456,'l':987654321,'f':1.0,'d':56.78}}");
        then(result).isEqualTo("okay");
    }

    private static class PrimitiveWrapperTypesClass {
        Boolean b = true;
        Character c = 'a';
        Byte y = 0x7;
        Short s = 0xff;
        Integer i = 123456;
        Long l = 987654321L;
        Float f = 1.0f;
        Double d = 56.78d;

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            PrimitiveWrapperTypesClass that = (PrimitiveWrapperTypesClass) o;
            return this.b.equals(that.b)
                    && this.c.equals(that.c)
                    && this.y.equals(that.y)
                    && this.s.equals(that.s)
                    && this.i.equals(that.i)
                    && this.l.equals(that.l)
                    && this.f.equals(that.f)
                    && this.d.equals(that.d);
        }

        @Override
        public int hashCode() {
            return Objects.hash(b, c, y, s, i, l, f, d);
        }
    }

    @GraphQlClientApi
    interface MutationWithPrimitiveWrappersApi {
        @Mutation
        String run(PrimitiveWrapperTypesClass primitives);
    }

    @Test
    public void shouldCallMutationWithPrimitiveWrappers() {
        fixture.returnsData("'run':'okay'");
        MutationWithPrimitiveWrappersApi api = fixture.builder().build(MutationWithPrimitiveWrappersApi.class);

        String result = api.run(new PrimitiveWrapperTypesClass());

        then(fixture.mutation()).isEqualTo("run(primitives: $primitives)");
        then(fixture.variables()).isEqualTo("{'primitives':{'b':true,'c':'a','y':7,'s':255,'i':123456,'l':987654321,'f':1.0,'d':56.78}}");
        then(result).isEqualTo("okay");
    }
}
