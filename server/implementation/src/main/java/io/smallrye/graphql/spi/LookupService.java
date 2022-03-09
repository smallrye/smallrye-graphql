package io.smallrye.graphql.spi;

import static io.smallrye.graphql.SmallRyeGraphQLServerLogging.log;
import static io.smallrye.graphql.SmallRyeGraphQLServerMessages.msg;

import java.lang.reflect.InvocationTargetException;
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
        LookupService lookupService;
        try {
            lookupService = lookupServices.iterator().next();
        } catch (Exception ex) {
            lookupService = new DefaultLookupService();
        }
        log.usingLookupService(lookupService.getName());
        return lookupService;
    }

    String getName();

    Class<?> getClass(Class<?> declaringClass);

    /**
     * Obtain an instance of the requested class. Don't forget to call `destroyIfNecessary()` on it after use
     * to avoid leaks!
     */
    <T> ManagedInstance<T> getInstance(Class<T> declaringClass);

    boolean isResolvable(Class<?> declaringClass);

    /**
     * Default Lookup service that gets used when none is provided with SPI.
     * This use reflection
     */
    class DefaultLookupService implements LookupService {

        @Override
        public String getName() {
            return "Reflection (default)";
        }

        @Override
        public Class<?> getClass(Class<?> declaringClass) {
            return declaringClass;
        }

        @Override
        public <T> ManagedInstance<T> getInstance(Class<T> declaringClass) {
            try {
                return new DefaultManagedInstance<T>(declaringClass.getConstructor().newInstance());
            } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
                    | IllegalArgumentException | InvocationTargetException ex) {
                throw msg.countNotGetInstance(ex);
            }
        }

        @Override
        public boolean isResolvable(Class<?> declaringClass) {
            return true;
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

        @Override
        public void destroyIfNecessary() {
            // nothing
        }
    }
}
