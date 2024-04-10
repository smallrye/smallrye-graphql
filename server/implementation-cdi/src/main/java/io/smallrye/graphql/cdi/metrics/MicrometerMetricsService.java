package io.smallrye.graphql.cdi.metrics;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.jboss.logging.Logger;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import io.smallrye.graphql.api.Context;
import io.smallrye.graphql.schema.model.OperationType;
import io.smallrye.graphql.spi.MetricsService;

public class MicrometerMetricsService implements MetricsService {
    private final MeterRegistry meterRegistry = Metrics.globalRegistry;
    private final Map<Long, MetricMeasurement<Timer.Sample>> metricsMemory = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> subscriptions = new ConcurrentHashMap<>();

    private static final String METRIC_NAME = "mp_graphql";
    private static final String METRIC_SUBSCRIPTIONS = "mp_graphql_subscription";
    private Logger LOG = Logger.getLogger(MicrometerMetricsService.class);

    public MicrometerMetricsService() {
        // If Micrometer is not available, this will throw an exception
        // and make sure that this service doesn't get registered
        meterRegistry.getMeters();
    }

    private Tags getTags(MetricMeasurement<Timer.Sample> metricMeasurement) {
        return Tags.of("name", metricMeasurement.getName())
                .and("type", metricMeasurement.getOperationType())
                .and("source", String.valueOf(metricMeasurement.isSource()));
    }

    @Override
    public void start(Long measurementId, Context context) {
        metricsMemory.put(measurementId,
                new MetricMeasurement<>(
                        context.getFieldName(),
                        context.hasSource(),
                        context.getOperationType(),
                        Timer.start()));
        LOG.tracef("(" + measurementId + ") Started recording metrics for: %s", context.getFieldName());
    }

    @Override
    public void end(Long measurementId) {
        MetricMeasurement<Timer.Sample> metricMeasurement = metricsMemory.remove(measurementId);
        Timer timer = meterRegistry.timer(METRIC_NAME, getTags(metricMeasurement));
        metricMeasurement.getMetric().stop(timer);
        LOG.tracef("(" + measurementId + ") Finished recording metrics for: %s", metricMeasurement.getName());
    }

    @Override
    public void subscriptionStart(Context context) {
        if (!OperationType.SUBSCRIPTION.name().equals(context.getOperationType())) {
            return;
        }
        subscriptions.computeIfAbsent(context.getFieldName(), k -> new AtomicLong(0));
        subscriptions.get(context.getFieldName()).incrementAndGet();
        meterRegistry.gauge(
                METRIC_SUBSCRIPTIONS,
                Tags.of("name", context.getFieldName()),
                subscriptions.get(context.getFieldName()),
                AtomicLong::get);
    }

    @Override
    public void subscriptionEnd(Context context) {
        if (!OperationType.SUBSCRIPTION.name().equals(context.getOperationType())) {
            return;
        }
        Optional.ofNullable(subscriptions.get(context.getFieldName())).ifPresent(AtomicLong::decrementAndGet);
    }
}
