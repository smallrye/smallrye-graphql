package io.smallrye.graphql.client.typesafe.impl.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

/** A static constructor method or a normal constructor */
@RequiredArgsConstructor
public class ConstructingInfo {
    private final Executable executable;

    @SneakyThrows(ReflectiveOperationException.class)
    public Object execute(Object... args) {
        if (executable instanceof Method) {
            return ((Method) executable).invoke(null, args);
        } else {
            return ((Constructor<?>) executable).newInstance(args);
        }
    }
}
