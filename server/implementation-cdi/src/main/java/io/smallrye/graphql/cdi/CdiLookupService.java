package io.smallrye.graphql.cdi;

import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.AmbiguousResolutionException;
import javax.enterprise.inject.UnsatisfiedResolutionException;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.CDI;

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
        CDI<Object> cdi = CDI.current();
        Bean<?> bean = getExactlyOneObject(cdi.getBeanManager().getBeans(declaringClass));
        boolean isDependentScope = bean.getScope().equals(Dependent.class);
        return new CDIManagedInstance<>(cdi.select(declaringClass), isDependentScope);
    }

    private <T> T getExactlyOneObject(Set<T> set) {
        if (set.size() > 1) {
            throw new AmbiguousResolutionException();
        }
        if (set.size() == 0) {
            throw new UnsatisfiedResolutionException();
        }
        return set.iterator().next();
    }

    @Override
    public boolean isResolvable(Class<?> declaringClass) {
        return CDI.current().select(declaringClass).isResolvable();
    }
}
