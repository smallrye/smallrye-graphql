package io.smallrye.graphql.cdi.metrics;

import java.time.Duration;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;

import javax.enterprise.inject.spi.CDI;
import javax.enterprise.util.AnnotationLiteral;

import org.eclipse.microprofile.metrics.Metadata;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.MetricType;
import org.eclipse.microprofile.metrics.Tag;
import org.eclipse.microprofile.metrics.annotation.RegistryType;

import io.smallrye.graphql.api.Context;
import io.smallrye.graphql.cdi.config.ConfigKey;
import io.smallrye.graphql.schema.model.Operation;
import io.smallrye.graphql.spi.EventingService;

/**
 * Listening for event and create metrics from it
 * 
 * @author Jan Martiska (jmartisk@redhat.com)
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class MetricsService implements EventingService {

    private MetricRegistry metricRegistry;
    private final Map<Context, Long> startTimes = Collections.synchronizedMap(new IdentityHashMap<>());
    private static final String METRIC_NAME = "mp_graphql";
    private final String DESCRIPTION = "Call statistics for the operation denoted by the 'name' tag";

    @Override
    public Operation createOperation(Operation operation) {
        final Tag[] tags = getTags(operation);

        Metadata metadata = Metadata.builder()
                .withName(METRIC_NAME)
                .withType(MetricType.SIMPLE_TIMER)
                .withDescription(DESCRIPTION)
                .build();
        getMetricRegistry().simpleTimer(metadata, tags);
        return operation;
    }

    @Override
    public void beforeDataFetch(Context context) {
        startTimes.put(context, System.nanoTime());
    }

    @Override
    public void afterDataFetch(Context context) {
        Long startTime = startTimes.remove(context);
        if (startTime != null) {
            long duration = System.nanoTime() - startTime;
            getMetricRegistry().simpleTimer(METRIC_NAME, getTags(context))
                    .update(Duration.ofNanos(duration));
        }
    }

    @Override
    public String getConfigKey() {
        return ConfigKey.ENABLE_METRICS;
    }

    private MetricRegistry getMetricRegistry() {
        if (metricRegistry == null) {
            this.metricRegistry = CDI.current().select(MetricRegistry.class, new VendorType()).get();
        }
        return metricRegistry;
    }

    private Tag[] getTags(Context context) {
        return new Tag[] {
                new Tag("name", context.getFieldName()),
                new Tag("type", context.getOperationType()),
                new Tag("source", String.valueOf(context.getSource() != null))
        };
    }

    private Tag[] getTags(Operation operation) {
        return new Tag[] {
                new Tag("name", operation.getName()),
                new Tag("type", operation.getOperationType().toString()),
                new Tag("source", String.valueOf(operation.isSourceField()))
        };
    }

    class VendorType extends AnnotationLiteral<RegistryType> implements RegistryType {
        @Override
        public MetricRegistry.Type type() {
            return MetricRegistry.Type.VENDOR;
        }
    }
}
