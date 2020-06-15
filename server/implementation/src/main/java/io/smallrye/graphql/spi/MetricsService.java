package io.smallrye.graphql.spi;

import static io.smallrye.graphql.SmallRyeGraphQLServerLogging.log;
import static io.smallrye.graphql.SmallRyeGraphQLServerMessages.msg;

import java.util.ServiceLoader;

import org.eclipse.microprofile.metrics.MetricRegistry;

/**
 * Service that allows containers to plug in their own MP Metrics implementation.
 */
public interface MetricsService {

    static MetricsService load() {
        MetricsService metricsService;
        try {
            ServiceLoader<MetricsService> sl = ServiceLoader.load(MetricsService.class);
            metricsService = sl.iterator().next();
        } catch (Exception ex) {
            metricsService = new DefaultMetricsService();
        }
        log.usingMetricsService(metricsService.getName());
        return metricsService;
    }

    String getName();

    MetricRegistry getMetricRegistry(MetricRegistry.Type type);

    /**
     * Default Metrics service that throws an UnsupportedOperationException.
     */
    class DefaultMetricsService implements MetricsService {

        @Override
        public String getName() {
            return "Unsupported Metrics Service";
        }

        @Override
        public MetricRegistry getMetricRegistry(MetricRegistry.Type type) {
            throw msg.metricsNotSupportedWithoutCDI();
        }
    }
}
