package io.smallrye.graphql.lookup;

import java.lang.reflect.InvocationTargetException;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import org.jboss.logging.Logger;

import io.smallrye.graphql.execution.Classes;

/**
 * Lookup service that allows multiple DI frameworks to use this.
 * By default, plain old reflection will be used.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Andy McCright (andymc@us.ibm.com)
 */
public interface LookupService {
    static final Logger LOG = Logger.getLogger(LookupService.class.getName());
    static final Map<String, Class> loadedClasses = new HashMap<>();

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
        try {
            if (Classes.isPrimitive(className)) {
                return Classes.getPrimativeClassType(className);
            } else {
                if (loadedClasses.containsKey(className)) {
                    return loadedClasses.get(className);
                } else {

                    return AccessController.doPrivileged((PrivilegedExceptionAction<Class<?>>) () -> {
                        ClassLoader loader = Thread.currentThread().getContextClassLoader();
                        if (loader != null) {
                            try {
                                return loadClass(className, loader);
                            } catch (ClassNotFoundException cnfe) {
                                // Let's try this class classloader.
                            }
                        }
                        return loadClass(className, Classes.class.getClassLoader());
                    });

                }
            }
        } catch (PrivilegedActionException | ClassNotFoundException pae) {
            throw new RuntimeException("Can not load class [" + className + "]", pae);
        }

    }

    default Class<?> loadClass(String className, ClassLoader loader) throws ClassNotFoundException {
        Class<?> c = Class.forName(className, false, loader);
        loadedClasses.put(className, c);
        return c;
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
