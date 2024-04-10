package io.smallrye.graphql.execution.metrics;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.ThreadLocalRandom;

import org.jboss.logging.Logger;

import io.smallrye.graphql.api.Context;
import io.smallrye.graphql.config.ConfigKey;
import io.smallrye.graphql.spi.MetricsService;
import io.smallrye.graphql.spi.config.Config;

public class MetricsEmitter {

    private static final Logger LOG = Logger.getLogger(MetricsEmitter.class);
    private static final ThreadLocal<MetricsEmitter> metricsEmitters = ThreadLocal.withInitial(MetricsEmitter::new);
    private final List<MetricsService> enabledServices;

    public static MetricsEmitter getInstance() {
        return metricsEmitters.get();
    }

    private MetricsEmitter() {
        Config config = Config.get();
        boolean enabled = config.getConfigValue(ConfigKey.ENABLE_METRICS, boolean.class, false);

        // Find out which metrics API is available
        ServiceLoader<MetricsService> metricService = ServiceLoader.load(MetricsService.class);
        Iterator<MetricsService> it = metricService.iterator();
        List<MetricsService> enabledServices = new ArrayList<>();

        while (enabled && it.hasNext()) {
            try {
                enabledServices.add(it.next());
            } catch (Throwable t) {
                // Ignore that service...
            }
        }

        this.enabledServices = enabledServices;
    }

    public void subscriptionStart(Context context) {
        enabledServices.forEach(metricsService -> metricsService.subscriptionStart(context));
    }

    public void subscriptionEnd(Context context) {
        enabledServices.forEach(metricsService -> metricsService.subscriptionEnd(context));
    }

    public Long start(Context context) {
        Long measurementId = ThreadLocalRandom.current().nextLong();
        enabledServices.forEach(metricsService -> metricsService.start(measurementId, context));
        return measurementId;
    }

    public void end(Long measurementId) {
        enabledServices.forEach(metricsService -> metricsService.end(measurementId));
    }
}
