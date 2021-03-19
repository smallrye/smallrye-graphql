package test.unit;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.List;

import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;
import org.junit.jupiter.api.Test;

import io.smallrye.graphql.client.typesafe.api.GraphQlClientApi;

class AnnotationBehavior {
    private final GraphQlClientFixture fixture = new GraphQlClientFixture();

    @GraphQlClientApi
    interface RenamedStringApi {
        @Query("greeting")
        String foo();
    }

    @Test
    void shouldCallRenamedStringQuery() {
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
    void shouldCallParamQuery() {
        fixture.returnsData("'greeting':'hi, foo'");
        RenamedParamApi api = fixture.build(RenamedParamApi.class);

        String greeting = api.greeting("foo");

        then(fixture.query()).isEqualTo("query greeting($who: String) { greeting(who: $who) }");
        then(fixture.variables()).isEqualTo("{'who':'foo'}");
        then(greeting).isEqualTo("hi, foo");
    }

    @GraphQlClientApi
    interface RenamedMethodApi {
        @Name("greeting")
        String someOtherMethodName();
    }

    @Test
    void shouldCallRenamedQuery() {
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
    void shouldCallObjectQuery() {
        fixture.returnsData("'greeting':{'foo':'foo','key':5}");
        ObjectApi api = fixture.build(ObjectApi.class);

        Greeting greeting = api.greeting();

        then(fixture.query()).isEqualTo("query greeting { greeting {foo key} }");
        then(greeting.text).isEqualTo("foo");
        then(greeting.code).isEqualTo(5);
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
