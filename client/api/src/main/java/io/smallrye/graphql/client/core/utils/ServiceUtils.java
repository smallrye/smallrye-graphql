package io.smallrye.graphql.client.core.utils;

import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class ServiceUtils {
    // Caches the resolved factory, not the ServiceLoader: a ServiceLoader is not safe for concurrent use
    private final static Map<Class<? extends Supplier<?>>, Supplier<?>> factories = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public static <T> T getNewInstanceFromFactory(Class<? extends Supplier<T>> clazz) {
        // computeIfAbsent does not store anything when the lookup throws, so a failed lookup can be retried
        Supplier<T> factory = (Supplier<T>) factories.computeIfAbsent(clazz, ServiceUtils::loadFactory);
        return factory.get();
    }

    private static <S extends Supplier<?>> S loadFactory(Class<S> clazz) {
        List<S> found = load(ServiceLoader.load(clazz));
        if (found.isEmpty()) {
            // The thread context classloader may not see the implementation, fall back to our own
            found = load(ServiceLoader.load(clazz, ServiceUtils.class.getClassLoader()));
        }
        if (found.size() != 1) {
            throw new IllegalArgumentException(
                    String.format("Expected exactly one implementation of %s. Found %d.", clazz.getName(),
                            found.size()));
        }
        return found.get(0);
    }

    private static <S extends Supplier<?>> List<S> load(ServiceLoader<S> serviceLoader) {
        return serviceLoader.stream().map(ServiceLoader.Provider::get).toList();
    }

    private ServiceUtils() {
        // HideUtilityClassConstructor
    }
}
