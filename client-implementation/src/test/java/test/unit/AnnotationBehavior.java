package test.unit;

import static org.assertj.core.api.BDDAssertions.then;

import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;
import org.junit.jupiter.api.Test;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

class AnnotationBehavior {
    private final GraphQlClientFixture fixture = new GraphQlClientFixture();

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

    interface ObjectApi {
        Greeting greeting();
    }

    @AllArgsConstructor
    @NoArgsConstructor(force = true)
    @Data
    static class Greeting {
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
        then(greeting).isEqualTo(new Greeting("foo", 5));
    }
}
