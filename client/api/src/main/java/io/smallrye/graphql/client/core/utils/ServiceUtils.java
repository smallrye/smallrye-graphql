package io.smallrye.graphql.client.core.utils;

import java.util.ServiceLoader;

public class ServiceUtils {
    // TODO: check if 0 or >1 instance found. Throw exception if either.
    public static <T> T getNewInstanceOf(Class<T> clazz) {
        ServiceLoader<T> sl = ServiceLoader.load(clazz);
        T t = sl.iterator().next();

        return t;
    }

    private ServiceUtils() {
        // HideUtilityClassConstructor
    }
}
