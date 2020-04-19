package io.smallrye.graphql.lookup;

import java.lang.reflect.InvocationTargetException;
import java.util.ServiceLoader;

import org.jboss.logging.Logger;

/**
 * Lookup service that allows multiple DI frameworks to use this.
 * By default, plain old reflection will be used.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Andy McCright (andymc@us.ibm.com)
 */
public interface LookupService {
    static final Logger LOG = Logger.getLogger(LookupService.class.getName());

    public static LookupService load() {
        LookupService lookupService;
        try {
            ServiceLoader<LookupService> sl = ServiceLoader.load(LookupService.class);
            lookupService = sl.iterator().next();
        } catch (Exception ex) {
            lookupService = new DefaultLookupService();
        }
        LOG.debug("Using " + lookupService.getName() + " lookup service");
        return lookupService;
    }

    String getName();

    Class<?> getClass(Class<?> declaringClass);

    Object getInstance(Class<?> declaringClass);

    default Object getInstance(String declaringClass) {
        Class<?> loadedClass = loadClass(declaringClass);
        return getInstance(loadedClass);
    }

    default Class<?> loadClass(String className) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();

        try {
            return Class.forName(className, false, loader);
        } catch (NullPointerException | ClassNotFoundException ex) {
            throw new RuntimeException("Could not load class [" + className + "] when using the default lookup service", ex);
        }

    }

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
        public Object getInstance(Class<?> declaringClass) {
            try {
                return declaringClass.getConstructor().newInstance();
            } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
                    | IllegalArgumentException | InvocationTargetException ex) {
                throw new RuntimeException("Could not get Instance using the default lookup service", ex);
            }
        }
    }
}
