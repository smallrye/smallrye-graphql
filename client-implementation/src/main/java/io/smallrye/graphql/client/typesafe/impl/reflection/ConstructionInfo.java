package io.smallrye.graphql.client.typesafe.impl.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;

/** A static constructor method or a normal constructor */
public class ConstructionInfo {
    private final Executable executable;

    public ConstructionInfo(Executable executable) { this.executable = executable; }

    public Object execute(Object... args) {
        try {
            if (executable instanceof Method) {
                return ((Method) executable).invoke(null, args);
            } else {
                return ((Constructor<?>) executable).newInstance(args);
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("can't invoke " + executable, e);
        }
    }
}
