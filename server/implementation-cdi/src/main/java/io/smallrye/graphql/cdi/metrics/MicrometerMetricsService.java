package io.smallrye.graphql.cdi.metrics;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.logging.Logger;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tags;
import io.smallrye.graphql.api.Context;
import io.smallrye.graphql.spi.MetricsService;

public class MicrometerMetricsService implements MetricsService {
    private final MeterRegistry meterRegistry = Metrics.globalRegistry;
    private final Map<Long, MetricMeasurement> metricsMemory = new ConcurrentHashMap<>();
    private static final String METRIC_NAME = "mp_graphql";
    private Logger LOG = Logger.getLogger(MicrometerMetricsService.class);

    public MicrometerMetricsService() {
        // If Micrometer is not available, this will throw an exception
        // and make sure that this service doesn't get registered
        meterRegistry.getMeters();
    }

    private Tags getTags(MetricMeasurement metricMeasurement) {
        return Tags.of("name", metricMeasurement.getName())
                .and("type", metricMeasurement.getOperationType())
                .and("source", String.valueOf(metricMeasurement.isSource()));
    }

    @Override
    public void start(Long measurementId, Context context) {
        metricsMemory.put(measurementId,
                new MetricMeasurement(
                        context.getFieldName(),
                        context.hasSource(),
                        context.getOperationType(),
                        System.nanoTime()));
        LOG.tracef("(" + measurementId + ") Started recording metrics for: %s", context.getFieldName());
    }

    @Override
    public void end(Long measurementId) {
        MetricMeasurement metricMeasurement = metricsMemory.remove(measurementId);
        long duration = System.nanoTime() - metricMeasurement.getTimeStarted();
        meterRegistry.timer(METRIC_NAME, getTags(metricMeasurement))
                .record(Duration.ofNanos(duration));
        LOG.tracef("(" + measurementId + ") Finished recording metrics for: %s", metricMeasurement.getName());
    }
}
