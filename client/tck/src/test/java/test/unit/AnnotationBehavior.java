package test.unit;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.List;

import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;
import org.junit.jupiter.api.Test;

import io.smallrye.graphql.client.typesafe.api.GraphQlClientApi;
import io.smallrye.graphql.client.typesafe.api.Multiple;

class AnnotationBehavior {
    private final GraphQlClientFixture fixture = new GraphQlClientFixture();

    @GraphQlClientApi
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

    @GraphQlClientApi
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

    @GraphQlClientApi
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

    @GraphQlClientApi
    interface ObjectApi {
        Greeting greeting();
    }

    private static class Greeting {
        @Name("foo")
        String text;
        @Name("key")
        int code;
    }

    @Test
    void shouldQueryObjectWithRenamedFields() {
        fixture.returnsData("'greeting':{'text':'foo','code':5}");
        ObjectApi api = fixture.build(ObjectApi.class);

        Greeting greeting = api.greeting();

        then(fixture.query()).isEqualTo("query greeting { greeting {text:foo code:key} }");
        then(greeting.text).isEqualTo("foo");
        then(greeting.code).isEqualTo(5);
    }

    @GraphQlClientApi
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

    @GraphQlClientApi
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
}
