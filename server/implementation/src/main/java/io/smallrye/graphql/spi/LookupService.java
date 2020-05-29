package io.smallrye.graphql.spi;

import java.lang.reflect.InvocationTargetException;
import java.util.ServiceLoader;

import io.smallrye.graphql.SmallRyeGraphQLServerLogging;
import io.smallrye.graphql.SmallRyeGraphQLServerMessages;

/**
 * Lookup service that allows multiple DI frameworks to use this.
 * By default, plain old reflection will be used.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @author Andy McCright (andymc@us.ibm.com)
 */
public interface LookupService {

    static LookupService load() {
        LookupService lookupService;
        try {
            ServiceLoader<LookupService> sl = ServiceLoader.load(LookupService.class);
            lookupService = sl.iterator().next();
        } catch (Exception ex) {
            lookupService = new DefaultLookupService();
        }
        SmallRyeGraphQLServerLogging.log.usingLookupService(lookupService.getName());
        return lookupService;
    }

    String getName();

    Class<?> getClass(Class<?> declaringClass);

    Object getInstance(Class<?> declaringClass);

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
                throw SmallRyeGraphQLServerMessages.msg.countNotGetInstance(ex);
            }
        }
    }
}
