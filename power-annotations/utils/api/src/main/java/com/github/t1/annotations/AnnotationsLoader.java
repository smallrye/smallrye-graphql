package com.github.t1.annotations;

import java.util.Iterator;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

/**
 * SPI + Singleton for loading the implementation
 */
public abstract class AnnotationsLoader {
    static final AnnotationsLoader INSTANCE = load();

    private static AnnotationsLoader load() {
        ServiceLoader<AnnotationsLoader> loader = ServiceLoader.load(AnnotationsLoader.class);
        Iterator<AnnotationsLoader> iterator = loader.iterator();
        if (!iterator.hasNext())
            throw new ServiceConfigurationError("no " + AnnotationsLoader.class.getName() + " in classpath");
        AnnotationsLoader graphQlClientBuilder = iterator.next();
        if (iterator.hasNext())
            throw new ServiceConfigurationError("more than one " + AnnotationsLoader.class.getName() + " in classpath");
        return graphQlClientBuilder;
    }

    public abstract Annotations onType(Class<?> type);

    public abstract Annotations onField(Class<?> type, String fieldName);

    public abstract Annotations onMethod(Class<?> type, String methodName, Class<?>... argTypes);
}
