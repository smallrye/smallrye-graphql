package io.smallrye.graphql.cdi.metrics;

import io.smallrye.graphql.api.Context;
import io.smallrye.graphql.cdi.config.ConfigKey;
import io.smallrye.graphql.schema.model.Operation;
import io.smallrye.graphql.spi.EventingService;

/**
 * A wrapper that chooses an underlying service implementation for metrics
 * and delegates to that implementation. The implementation is chosen based on
 * detecting the availability of particular metrics APIs.
 * Right now, it supports Micrometer and MP Metrics 3.x, while Micrometer
 * is preferred.
 */
public class MetricsService implements EventingService {

    private final EventingService wrapped;

    public MetricsService() {
        // Find out which metrics API is available
        EventingService wrappedImpl;
        try {
            Class.forName("io.micrometer.core.instrument.MeterRegistry");
            wrappedImpl = new MicrometerMetricsService();
        } catch (ClassNotFoundException e) {
            try {
                Class.forName("org.eclipse.microprofile.metrics.MetricRegistry");
                wrappedImpl = new MPMetricsService();
            } catch (ClassNotFoundException classNotFoundException) {
                wrappedImpl = null;
            }
        }
        this.wrapped = wrappedImpl;
    }

    @Override
    public Operation createOperation(Operation operation) {
        return wrapped.createOperation(operation);
    }

    @Override
    public void beforeDataFetch(Context context) {
        wrapped.beforeDataFetch(context);
    }

    @Override
    public void afterDataFetch(Context context) {
        wrapped.afterDataFetch(context);
    }

    @Override
    public String getConfigKey() {
        return ConfigKey.ENABLE_METRICS;
    }
}
