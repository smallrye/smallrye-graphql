package io.smallrye.graphql.client.core.utils;

import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class ServiceUtils {
    private final static Map<Class<? extends Supplier<?>>, ServiceLoader<?>> cacheMemory = new ConcurrentHashMap<>();

    public static <T> T getNewInstanceFromFactory(Class<? extends Supplier<T>> clazz) {
        var sl = (ServiceLoader<? extends Supplier<T>>) cacheMemory.computeIfAbsent(clazz,
                ServiceLoader::load);
        if (sl.stream().count() != 1) {
            throw new IllegalArgumentException(
                    String.format("Expected exactly one implementation of %s. Found %d.", clazz.getName(),
                            sl.stream().count()));
        }
        return sl.iterator().next().get();
    }

    private ServiceUtils() {
        // HideUtilityClassConstructor
    }
}
