package test.unit;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.List;

import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;
import org.junit.jupiter.api.Test;

import io.smallrye.graphql.client.typesafe.api.GraphQlClientApi;

public class AnnotationBehavior {
    private final GraphQlClientFixture fixture = new GraphQlClientFixture();

    @GraphQlClientApi
    interface RenamedStringApi {
        @Query("greeting")
        String foo();
    }

    @Test
    public void shouldCallRenamedStringQuery() {
        fixture.returnsData("'greeting':'dummy-greeting'");
        RenamedStringApi api = fixture.builder().build(RenamedStringApi.class);

        String greeting = api.foo();

        then(fixture.query()).isEqualTo("greeting");
        then(greeting).isEqualTo("dummy-greeting");
    }

    @Test
    public void shouldIgnoreEmptyError() {
        fixture.returnsDataEmptyError("'greeting':'dummy-greeting'");
        RenamedStringApi api = fixture.builder().build(RenamedStringApi.class);

        String greeting = api.foo();

        then(fixture.query()).isEqualTo("greeting");
        then(greeting).isEqualTo("dummy-greeting");
    }

    @GraphQlClientApi
    interface RenamedParamApi {
        String greeting(@Name("who") String foo);
    }

    @Test
    public void shouldCallParamQuery() {
        fixture.returnsData("'greeting':'hi, foo'");
        RenamedParamApi api = fixture.builder().build(RenamedParamApi.class);

        String greeting = api.greeting("foo");

        then(fixture.query()).isEqualTo("greeting(who: 'foo')");
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
    public void shouldCallObjectQuery() {
        fixture.returnsData("'greeting':{'foo':'foo','key':5}");
        ObjectApi api = fixture.builder().build(ObjectApi.class);

        Greeting greeting = api.greeting();

        then(fixture.query()).isEqualTo("greeting {foo key}");
        then(greeting.text).isEqualTo("foo");
        then(greeting.code).isEqualTo(5);
    }

    private static class Thing {
        List<OtherThing> otherThings;
    }

    private static class OtherThing {
        String someValue;
    }

    @GraphQlClientApi
    interface ThingsApi {
        Thing things();
    }

    @Test
    public void shouldHandleUnannotatedContainerField() throws Exception {
        fixture.returnsData("'things': {'otherThings': [null]}");
        ThingsApi stuff = fixture.builder().build(ThingsApi.class);

        Thing things = stuff.things();

        then(things.otherThings).hasSize(1);
    }
}
