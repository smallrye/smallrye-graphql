package io.smallrye.graphql.schema.helper;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.jandex.MethodInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import io.smallrye.graphql.schema.IndexCreator;

class MethodHelperTest {

    @ParameterizedTest
    @ValueSource(strings = { "getValue", })
    void testGetter(String methodName) throws IOException {
        MethodInfo method = getFirstMethod(methodName);

        boolean actual = MethodHelper.isPropertyMethod(Direction.OUT, method);

        assertTrue(actual);
    }

    @ParameterizedTest
    @ValueSource(strings = { "getValueForKey", "getNothing", })
    void testNonGetter(String methodName) throws IOException {
        MethodInfo method = getFirstMethod(methodName);

        boolean actual = MethodHelper.isPropertyMethod(Direction.OUT, method);

        assertFalse(actual);
    }

    @ParameterizedTest
    @ValueSource(strings = { "setValue", })
    void testSetter(String methodName) throws IOException {
        MethodInfo method = getFirstMethod(methodName);

        boolean actual = MethodHelper.isPropertyMethod(Direction.IN, method);

        assertTrue(actual);
    }

    @ParameterizedTest
    @ValueSource(strings = { "setValueForKey", "setNothing", })
    void testNonSetter(String methodName) throws IOException {
        MethodInfo method = getFirstMethod(methodName);

        boolean actual = MethodHelper.isPropertyMethod(Direction.IN, method);

        assertFalse(actual);
    }

    private MethodInfo getFirstMethod(String methodName) throws IOException {
        Index complete = IndexCreator.index(PojoWithGetter.class);

        ClassInfo classByName = complete.getClassByName(DotName.createSimple(PojoWithGetter.class.getName()));
        MethodInfo method = classByName.firstMethod(methodName);

        return method;
    }
}
