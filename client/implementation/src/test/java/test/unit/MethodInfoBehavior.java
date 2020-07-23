package test.unit;

import static org.assertj.core.api.BDDAssertions.then;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import io.smallrye.graphql.client.typesafe.impl.reflection.MethodInfo;

public class MethodInfoBehavior {

    private String foo(String echo) {
        return "foo " + echo;
    }

    @Test
    public void canInvokePrivateMethod() throws Exception {
        Method foo = this.getClass().getDeclaredMethod("foo", new Class<?>[] { String.class });
        MethodInfo mi = MethodInfo.of(foo, "bar");
        then(mi.invoke(this, "bar")).isEqualTo("foo bar");
    }
}
