package io.smallrye.graphql.cdi.metrics;

import java.time.Duration;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import io.smallrye.graphql.api.Context;
import io.smallrye.graphql.cdi.config.ConfigKey;
import io.smallrye.graphql.schema.model.Operation;
import io.smallrye.graphql.spi.EventingService;

public class MicrometerMetricsService implements EventingService {

    private final MeterRegistry meterRegistry = Metrics.globalRegistry;
    private final Map<Context, Long> startTimes = Collections.synchronizedMap(new IdentityHashMap<>());
    private static final String METRIC_NAME = "mp_graphql";
    private final String DESCRIPTION = "Call statistics for the operation denoted by the 'name' tag";

    @Override
    public Operation createOperation(Operation operation) {
        final Tags tags = getTags(operation);
        Timer.builder(METRIC_NAME)
                .tags(tags)
                .description(DESCRIPTION)
                .register(meterRegistry);
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
            meterRegistry.timer(METRIC_NAME, getTags(context))
                    .record(Duration.ofNanos(duration));
        }
    }

    @Override
    public String getConfigKey() {
        return ConfigKey.ENABLE_METRICS;
    }

    private Tags getTags(Context context) {
        return Tags.of("name", context.getFieldName())
                .and("type", context.getOperationType())
                .and("source", String.valueOf(context.getSource() != null));
    }

    private Tags getTags(Operation operation) {
        return Tags.of("name", operation.getName())
                .and("type", operation.getOperationType().toString())
                .and("source", String.valueOf(operation.isSourceField()));
    }

}
