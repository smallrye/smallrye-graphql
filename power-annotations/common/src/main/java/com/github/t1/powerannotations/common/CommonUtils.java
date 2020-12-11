package com.github.t1.powerannotations.common;

import static java.util.stream.Collectors.joining;

import java.util.Arrays;
import java.util.stream.Stream;

import org.jboss.jandex.DotName;

public class CommonUtils {
    private CommonUtils() {
    }

    public static DotName toDotName(Class<?> type) {
        return toDotName(type.getName());
    }

    public static DotName toDotName(String typeName) {
        return DotName.createSimple(typeName);
    }

    public static String signature(String methodName, String... argTypeNames) {
        return methodName + Stream.of(argTypeNames).collect(joining(", ", "(", ")"));
    }

    public static <T> T[] with(T[] original, T newInstance) {
        T[] copy = Arrays.copyOf(original, original.length + 1);
        copy[original.length] = newInstance;
        return copy;
    }

    public static <T> void replace(T[] array, T original, T replacement) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == original) {
                array[i] = replacement;
                return;
            }
        }
        throw new RuntimeException("original element " + original + " not in array " + Arrays.toString(array));
    }
}
