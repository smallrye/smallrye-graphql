package io.smallrye.graphql.spi;

import static io.smallrye.graphql.SmallRyeGraphQLServerLogging.log;
import static io.smallrye.graphql.SmallRyeGraphQLServerMessages.msg;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Lookup service that allows multiple DI frameworks to use this.
 * By default, plain old reflection will be used.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Andy McCright (andymc@us.ibm.com)
 */
public interface LookupService {

    ServiceLoader<LookupService> lookupServices = ServiceLoader.load(LookupService.class);

    LookupService lookupService = load();

    static LookupService get() {
        return lookupService;
    }

    static LookupService load() {
        LookupService ls = null;
        Iterator<LookupService> iterator = lookupServices.iterator();
        while (iterator.hasNext()) {
            try {
                ls = iterator.next();
                String packageName = ls.getClass().getPackageName();
                // give more priority to user-supplied implementations - if we find one outside smallrye/quarkus
                // packages, use it
                if (!packageName.startsWith("io.smallrye") && !packageName.startsWith("io.quarkus")) {
                    break;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        if (ls == null) {
            ls = new DefaultLookupService();
        }

        log.usingLookupService(ls.getName());
        return ls;
    }

    String getName();

    default Class<?> getClass(Class<?> declaringClass) {
        return declaringClass;
    }

    /**
     * Obtain an instance of the requested class. Don't forget to call `destroyIfNecessary()` on it after use
     * to avoid leaks!
     */
    <T> ManagedInstance<T> getInstance(Class<T> declaringClass);

    default boolean isResolvable(Class<?> declaringClass) {
        return true;
    }

    /**
     * Default Lookup service that gets used when none is provided with SPI.
     * This use reflection
     */
    class DefaultLookupService implements LookupService {
        private final Map<Class, InheritableThreadLocal<ManagedInstance>> instanceMap = new HashMap<>();

        @Override
        public String getName() {
            return "Thread Local and reflection";
        }

        @Override
        public <T> ManagedInstance<T> getInstance(Class<T> declaringClass) {
            if (instanceMap.containsKey(declaringClass)) {
                return (ManagedInstance<T>) instanceMap.get(declaringClass).get();
            } else {
                ManagedInstance<T> newManagedInstance = getNewManagedInstance(declaringClass);
                InheritableThreadLocal<ManagedInstance> threadLocal = new InheritableThreadLocal<>();
                threadLocal.set(newManagedInstance);
                instanceMap.put(declaringClass, threadLocal);
                return getInstance(declaringClass);
            }
        }

        private <T> ManagedInstance<T> getNewManagedInstance(Class<T> declaringClass) {
            try {
                return new DefaultManagedInstance<T>(declaringClass.getConstructor().newInstance());
            } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
                    | IllegalArgumentException | InvocationTargetException ex) {
                throw msg.countNotGetInstance(ex);
            }
        }
    }

    class DefaultManagedInstance<T> implements ManagedInstance<T> {
        private final T instance;

        public DefaultManagedInstance(T instance) {
            this.instance = instance;
        }

        @Override
        public T get() {
            return instance;
        }
    }
}
