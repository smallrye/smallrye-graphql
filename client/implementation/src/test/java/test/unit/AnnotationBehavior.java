package test.unit;

import static org.assertj.core.api.BDDAssertions.then;

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
    void shouldCallParamQuery() {
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
    void shouldCallObjectQuery() {
        fixture.returnsData("'greeting':{'foo':'foo','key':5}");
        ObjectApi api = fixture.builder().build(ObjectApi.class);

        Greeting greeting = api.greeting();

        then(fixture.query()).isEqualTo("greeting {foo key}");
        then(greeting.text).isEqualTo("foo");
        then(greeting.code).isEqualTo(5);
    }
}
