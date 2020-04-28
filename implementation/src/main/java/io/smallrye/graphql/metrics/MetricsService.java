package io.smallrye.graphql.metrics;

import java.util.ServiceLoader;

import org.eclipse.microprofile.metrics.MetricRegistry;
import org.jboss.logging.Logger;

/**
 * Service that allows containers to plug in their own MP Metrics implementation.
 */
public interface MetricsService {
    static final Logger LOG = Logger.getLogger(MetricsService.class.getName());

    public static MetricsService load() {
        MetricsService metricsService;
        try {
            ServiceLoader<MetricsService> sl = ServiceLoader.load(MetricsService.class);
            metricsService = sl.iterator().next();
        } catch (Exception ex) {
            metricsService = new DefaultMetricsService();
        }
        LOG.debug("Using " + metricsService.getName() + " lookup service");
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
            throw new UnsupportedOperationException("Metrics are not supported without CDI");
        }
    }
}