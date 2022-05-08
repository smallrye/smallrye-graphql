package tck.graphql.typesafe;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.List;

import jakarta.json.bind.annotation.JsonbProperty;

import org.eclipse.microprofile.graphql.Ignore;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;
import org.junit.jupiter.api.Test;

import io.smallrye.graphql.client.typesafe.api.GraphQLClientApi;
import io.smallrye.graphql.client.typesafe.api.Multiple;

class AnnotationBehavior {
    private final TypesafeGraphQLClientFixture fixture = TypesafeGraphQLClientFixture.load();

    @GraphQLClientApi
    interface RenamedStringApi {
        @Query("greeting")
        String foo();
    }

    @Test
    void shouldQueryRenamedString() {
        fixture.returnsData("'greeting':'dummy-greeting'");
        RenamedStringApi api = fixture.build(RenamedStringApi.class);

        String greeting = api.foo();

        then(fixture.query()).isEqualTo("query greeting { greeting }");
        then(greeting).isEqualTo("dummy-greeting");
    }

    @GraphQLClientApi
    interface RenamedParamApi {
        String greeting(@Name("who") String foo);
    }

    @Test
    void shouldQueryNamedParam() {
        fixture.returnsData("'greeting':'hi, foo'");
        RenamedParamApi api = fixture.build(RenamedParamApi.class);

        String greeting = api.greeting("foo");

        then(fixture.query()).isEqualTo("query greeting($foo: String) { greeting(who: $foo) }");
        then(fixture.variables()).isEqualTo("{'foo':'foo'}");
        then(greeting).isEqualTo("hi, foo");
    }

    @GraphQLClientApi
    interface RenamedMethodApi {
        @Name("greeting")
        String someOtherMethodName();
    }

    @Test
    void shouldQueryRenamedMethod() {
        fixture.returnsData("'greeting':'hi, foo'");
        RenamedMethodApi api = fixture.build(RenamedMethodApi.class);

        String greeting = api.someOtherMethodName();

        then(fixture.query()).isEqualTo("query greeting { greeting }");
        then(fixture.variables()).isEqualTo("{}");
        then(greeting).isEqualTo("hi, foo");
    }

    @GraphQLClientApi
    interface RenamedGreetingApi {
        RenamedGreeting greeting();
    }

    private static class RenamedGreeting {
        @Name("foo")
        String text;
        @Name("key")
        int code;
    }

    @Test
    void shouldQueryObjectWithRenamedFields() {
        fixture.returnsData("'greeting':{'text':'foo','code':5}");
        RenamedGreetingApi api = fixture.build(RenamedGreetingApi.class);

        RenamedGreeting greeting = api.greeting();

        then(fixture.query()).isEqualTo("query greeting { greeting {text:foo code:key} }");
        then(greeting.text).isEqualTo("foo");
        then(greeting.code).isEqualTo(5);
    }

    @GraphQLClientApi
    interface GreetingWithIgnoreApi {
        GreetingWithIgnore greeting(GreetingWithIgnore in);
    }

    private static class GreetingWithIgnore {
        String text;
        @Ignore
        String ignore;

        @SuppressWarnings("unused")
        GreetingWithIgnore() {
        }

        GreetingWithIgnore(String text, String ignore) {
            this.text = text;
            this.ignore = ignore;
        }
    }

    @Test
    void shouldQueryObjectWithFieldsWithIgnore() {
        fixture.returnsData("'greeting':{'text':'foo'}");
        GreetingWithIgnoreApi api = fixture.build(GreetingWithIgnoreApi.class);

        GreetingWithIgnore greeting = api.greeting(new GreetingWithIgnore("foo", "the-code"));

        then(fixture.query()).isEqualTo("query greeting($in: GreetingWithIgnoreInput) { greeting(in: $in) {text} }");
        then(greeting.text).isEqualTo("foo");
        then(greeting.ignore).isNull();
    }

    @GraphQLClientApi
    interface HeroesAPI {
        HeroAndVillain heroAndVillain();

        HeroPair randomHeroPair();
    }

    static class Hero {
        String name;
        boolean good;
    }

    static class Villain {
        String name;
    }

    @Multiple
    static class HeroAndVillain {
        Hero hero;
        Villain villain;
    }

    @Multiple
    static class HeroPair {
        @Name("hero")
        Hero left;
        @Name("hero")
        Hero right;
    }

    @Test
    void shouldCallMultipleWithoutParamsDifferentType() {
        fixture.returnsData("'hero':{'name':'Spider-Man','good':true},'villain':{'name':'Venom'}");
        HeroesAPI api = fixture.build(HeroesAPI.class);

        HeroAndVillain heroAndVillain = api.heroAndVillain();

        then(fixture.query()).isEqualTo("query heroAndVillain {hero {name good} villain {name}}");
        then(fixture.variables()).isEqualTo("{}");
        then(heroAndVillain.hero.name).isEqualTo("Spider-Man");
        then(heroAndVillain.hero.good).isTrue();
        then(heroAndVillain.villain.name).isEqualTo("Venom");
    }

    @Test
    void shouldCallMultipleWithoutParamsSameType() {
        fixture.returnsData("'left':{'name':'Spider-Man','good':true},'right':{'name':'Venom','good':false}");
        HeroesAPI api = fixture.build(HeroesAPI.class);

        HeroPair pair = api.randomHeroPair();

        then(fixture.query()).isEqualTo("query randomHeroPair {left:hero {name good} right:hero {name good}}");
        then(fixture.variables()).isEqualTo("{}");
        then(pair.left.name).isEqualTo("Spider-Man");
        then(pair.left.good).isTrue();
        then(pair.right.name).isEqualTo("Venom");
        then(pair.right.good).isFalse();
    }

    private static class Thing {
        List<OtherThing> otherThings;
    }

    private static class OtherThing {
        @SuppressWarnings("unused")
        String someValue;
    }

    @GraphQLClientApi
    interface ThingsApi {
        Thing things();
    }

    @Test
    void shouldHandleUnannotatedContainerField() {
        fixture.returnsData("'things': {'otherThings': [null]}");
        ThingsApi stuff = fixture.build(ThingsApi.class);

        Thing things = stuff.things();

        then(things.otherThings).hasSize(1);
    }

    @GraphQLClientApi
    interface NillableFieldsApi {
        String query(NillableFields param);
    }

    static class NillableFields {

        public NillableFields(String string, Integer integer) {
            this.string = string;
            this.integer = integer;
        }

        @SuppressWarnings("DefaultAnnotationParam")
        @JsonbProperty(nillable = false)
        String string;

        @JsonbProperty(nillable = true)
        Integer integer;

    }

    @Test
    void nillableFields() {
        fixture.returnsData("'query': 'blabla'");
        NillableFieldsApi client = fixture.build(NillableFieldsApi.class);
        NillableFields param = new NillableFields(null, null);

        String res = client.query(param);

        then(fixture.variables()).doesNotContain("'string'");
        then(fixture.variables()).contains("'integer':null");
        then(res).isEqualTo("blabla");
    }

    @GraphQLClientApi
    interface JsonbPropertyApi {
        String query(JsonbPropertyAnnotatedFields param);
    }

    static class JsonbPropertyAnnotatedFields {

        public JsonbPropertyAnnotatedFields(String aaa, String bbb) {
            this.aaa = aaa;
            this.bbb = bbb;
        }

        @JsonbProperty(value = "xxx")
        String aaa;

        @JsonbProperty(value = "yyy")
        String bbb;

    }

    @Test
    void fieldsRenamedByJsonbAnnotation() {
        fixture.returnsData("'query': 'blabla'");
        JsonbPropertyApi client = fixture.build(JsonbPropertyApi.class);
        JsonbPropertyAnnotatedFields param = new JsonbPropertyAnnotatedFields("123", "456");

        String res = client.query(param);

        then(fixture.variables()).contains("'xxx':'123'");
        then(fixture.variables()).contains("'yyy':'456'");
        then(res).isEqualTo("blabla");
    }
}
