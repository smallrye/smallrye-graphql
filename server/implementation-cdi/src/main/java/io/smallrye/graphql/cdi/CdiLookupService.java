package io.smallrye.graphql.cdi;

import jakarta.enterprise.inject.spi.CDI;

import io.smallrye.graphql.spi.LookupService;
import io.smallrye.graphql.spi.ManagedInstance;

/**
 * Lookup service that gets the beans via CDI
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class CdiLookupService implements LookupService {

    @Override
    public String getName() {
        return "CDI";
    }

    @Override
    public Class<?> getClass(Class<?> declaringClass) {
        Object declaringObject = getInstance(declaringClass);
        return declaringObject.getClass();
    }

    @Override
    public <T> ManagedInstance<T> getInstance(Class<T> declaringClass) {
        // getHandle() throws exception if there is unsatisfied or ambiguous dep
        return new CDIManagedInstance<>(CDI.current().getBeanContainer().createInstance().select(declaringClass).getHandle());
    }

    @Override
    public boolean isResolvable(Class<?> declaringClass) {
        return CDI.current().select(declaringClass).isResolvable();
    }
}
