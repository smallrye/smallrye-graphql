package test.unit;

import static org.assertj.core.api.BDDAssertions.then;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import io.smallrye.graphql.client.typesafe.api.GraphQlClientApi;
import io.smallrye.graphql.client.typesafe.impl.reflection.MethodInvocation;

class InvocationBehavior {
    private final GraphQlClientFixture fixture = new GraphQlClientFixture();

    private String foo(String echo) {
        return "foo " + echo;
    }

    @Test
    void canInvokePrivateMethod() throws Exception {
        Method foo = this.getClass().getDeclaredMethod("foo", String.class);
        MethodInvocation mi = MethodInvocation.of(foo, "bar");
        then(mi.invoke(this)).isEqualTo("foo bar");
    }

    @GraphQlClientApi
    interface CloseableApi extends Closeable {
        @SuppressWarnings("unused")
        String foo();
    }

    @Test
    void shouldCloseClientNotCall() throws IOException {
        CloseableApi api = fixture.build(CloseableApi.class);

        api.close();

        fixture.verifyClosed();
    }

    @Test
    void shouldNotInvokeToString() {
        CloseableApi api = fixture.build(CloseableApi.class);

        String actual = api.toString();

        then(actual).isNotNull();
    }

    @Test
    void shouldNotInvokeEquals() {
        CloseableApi api = fixture.build(CloseableApi.class);

        @SuppressWarnings("EqualsBetweenInconvertibleTypes")
        boolean equals = api.equals(this);

        then(equals).isFalse();
    }

    @Test
    void shouldNotInvokeHashCode() {
        CloseableApi api = fixture.build(CloseableApi.class);

        int hashCode = api.hashCode();

        then(hashCode).isNotNull();
    }
}
