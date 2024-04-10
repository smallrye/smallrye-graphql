package io.smallrye.graphql.cdi.metrics;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.enterprise.util.AnnotationLiteral;

import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.Tag;
import org.eclipse.microprofile.metrics.annotation.RegistryType;
import org.jboss.logging.Logger;

import io.smallrye.graphql.api.Context;
import io.smallrye.graphql.schema.model.OperationType;
import io.smallrye.graphql.spi.MetricsService;

/**
 * Listening for event and create metrics from it. Uses MP Metrics 3.x API.
 *
 * @author Jan Martiska (jmartisk@redhat.com)
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class MPMetricsService implements MetricsService {

    private MetricRegistry metricRegistry;
    private final Map<Long, MetricMeasurement<Long>> metricsMemory = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> subscriptions = new ConcurrentHashMap<>();
    private static final String METRIC_NAME = "mp_graphql";
    private static final String METRIC_SUBSCRIPTIONS = "mp_graphql_subscription";
    private Logger LOG = Logger.getLogger(MPMetricsService.class);

    public MPMetricsService() {
        // If MP Metrics are not available, this will throw an exception
        // and make sure that this service doesn't get registered
        getMetricRegistry();
    }

    private MetricRegistry getMetricRegistry() {
        if (metricRegistry == null) {
            this.metricRegistry = CDI.current().select(MetricRegistry.class, new VendorType()).get();
        }
        return metricRegistry;
    }

    private Tag[] getTags(MetricMeasurement<Long> metricMeasurement) {
        return new Tag[] {
                new Tag("name", metricMeasurement.getName()),
                new Tag("type", metricMeasurement.getOperationType()),
                new Tag("source", String.valueOf(metricMeasurement.isSource()))
        };
    }

    @Override
    public void start(Long measurementId, Context context) {
        metricsMemory.put(measurementId, new MetricMeasurement<>(context.getFieldName(),
                context.hasSource(),
                context.getOperationType(),
                System.nanoTime()));
        LOG.tracef("(" + measurementId + ") Started recording metrics for: %s", context.getFieldName());
    }

    @Override
    public void end(Long measurementId) {
        MetricMeasurement<Long> metricMeasurement = metricsMemory.remove(measurementId);
        long duration = System.nanoTime() - metricMeasurement.getMetric();
        getMetricRegistry().simpleTimer(METRIC_NAME, getTags(metricMeasurement))
                .update(Duration.ofNanos(duration));
        LOG.tracef("(" + measurementId + ") Finished recording metrics for: %s", metricMeasurement.getName());
    }

    @Override
    public void subscriptionStart(Context context) {
        if (!OperationType.SUBSCRIPTION.name().equals(context.getOperationType())) {
            return;
        }
        subscriptions.computeIfAbsent(context.getFieldName(), k -> new AtomicLong(0));
        subscriptions.get(context.getFieldName()).incrementAndGet();
        getMetricRegistry().gauge(
                METRIC_SUBSCRIPTIONS,
                () -> subscriptions.get(context.getFieldName()).get(),
                new Tag("name", context.getFieldName()));
    }

    @Override
    public void subscriptionEnd(Context context) {
        if (!OperationType.SUBSCRIPTION.name().equals(context.getOperationType())) {
            return;
        }
        Optional.ofNullable(subscriptions.get(context.getFieldName())).ifPresent(AtomicLong::decrementAndGet);
    }

    class VendorType extends AnnotationLiteral<RegistryType> implements RegistryType {
        @Override
        public MetricRegistry.Type type() {
            return MetricRegistry.Type.VENDOR;
        }
    }
}
