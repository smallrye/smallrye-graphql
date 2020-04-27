package io.smallrye.graphql.execution.datafetcher.decorator;

import java.time.Duration;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;

import org.eclipse.microprofile.metrics.MetricID;
import org.eclipse.microprofile.metrics.MetricRegistry;

import graphql.schema.DataFetchingEnvironment;
import io.smallrye.graphql.metrics.MetricsService;

public class MetricDecorator implements DataFetcherDecorator {

    private final Map<DataFetchingEnvironment, Long> startTimes = Collections.synchronizedMap(new IdentityHashMap<>());

    private final MetricRegistry metricRegistry;

    public MetricDecorator() {
        metricRegistry = MetricsService.load().getMetricRegistry(MetricRegistry.Type.VENDOR);
    }

    @Override
    public void before(DataFetchingEnvironment dfe) {
        startTimes.put(dfe, System.nanoTime());
    }

    @Override
    public void after(DataFetchingEnvironment dfe) {
        Long startTime = startTimes.remove(dfe);
        if (startTime != null) {
            long duration = System.nanoTime() - startTime;
            MetricID metricID = new MetricID("mp_graphql_" + dfe.getField().getName());
            metricRegistry.simpleTimer(metricID.getName(), metricID.getTagsAsArray())
                    .update(Duration.ofNanos(duration));
        }

    }

}
