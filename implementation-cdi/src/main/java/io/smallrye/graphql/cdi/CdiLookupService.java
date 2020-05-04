package io.smallrye.graphql.cdi;

import javax.enterprise.inject.spi.CDI;

import org.eclipse.microprofile.metrics.MetricRegistry;

import io.smallrye.graphql.spi.LookupService;
import io.smallrye.graphql.spi.MetricsService;

/**
 * Lookup service that gets the beans via CDI
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class CdiLookupService implements LookupService, MetricsService {

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
    public MetricRegistry getMetricRegistry(MetricRegistry.Type type) {
        return CDI.current().select(MetricRegistry.class, new RegistryTypeLiteral(type)).get();
    }

}
