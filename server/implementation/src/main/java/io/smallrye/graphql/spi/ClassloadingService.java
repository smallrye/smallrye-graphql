package io.smallrye.graphql.spi;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ServiceLoader;

import org.jboss.logging.Logger;

import io.smallrye.graphql.execution.Classes;

/**
 * Classloading service that will load classes
 * By default, TCCL will be use.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Andy McCright (andymc@us.ibm.com)
 */
public interface ClassloadingService {
    static final Logger LOG = Logger.getLogger(ClassloadingService.class.getName());

    public static ClassloadingService load() {
        ClassloadingService classloadingService;
        try {
            ServiceLoader<ClassloadingService> sl = ServiceLoader.load(ClassloadingService.class);
            classloadingService = sl.iterator().next();
        } catch (Exception ex) {
            classloadingService = new DefaultClassloadingService();
        }
        LOG.debug("Using " + classloadingService.getName() + " classloading service");
        return classloadingService;
    }

    String getName();

    default Class<?> loadClass(String className) {
        try {
            if (Classes.isPrimitive(className)) {
                return Classes.getPrimativeClassType(className);
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
                    return loadClass(className, ClassloadingService.class.getClassLoader());
                });
            }
        } catch (PrivilegedActionException | ClassNotFoundException pae) {
            throw new RuntimeException("Can not load class [" + className + "]", pae);
        }
    }

    default Class<?> loadClass(String className, ClassLoader loader) throws ClassNotFoundException {
        Class<?> c = Class.forName(className, false, loader);
        return c;
    }

    /**
     * Default Lookup service that gets used when none is provided with SPI.
     * This use reflection
     */
    class DefaultClassloadingService implements ClassloadingService {

        @Override
        public String getName() {
            return "TCCL (default)";
        }
    }
}
