package io.smallrye.graphql.cdi;

import javax.enterprise.util.AnnotationLiteral;

import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.annotation.RegistryType;

public class RegistryTypeLiteral extends AnnotationLiteral<RegistryType> implements RegistryType {

    private final MetricRegistry.Type type;

    public RegistryTypeLiteral(MetricRegistry.Type type) {
        this.type = type;
    }

    @Override
    public MetricRegistry.Type type() {
        return type;
    }
}
