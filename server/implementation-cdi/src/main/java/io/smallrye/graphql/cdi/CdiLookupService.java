package io.smallrye.graphql.cdi;

import javax.enterprise.inject.spi.CDI;

import io.smallrye.graphql.spi.LookupService;

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
    public Object getInstance(Class<?> declaringClass) {
        return CDI.current().select(declaringClass).get();
    }

    @Override
    public boolean isResolvable(Class<?> declaringClass) {
        return CDI.current().select(declaringClass).isResolvable();
    }
}
