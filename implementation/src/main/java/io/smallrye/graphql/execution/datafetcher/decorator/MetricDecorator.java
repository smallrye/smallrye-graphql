package io.smallrye.graphql.execution.datafetcher.decorator;

import java.time.Duration;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;

import org.eclipse.microprofile.metrics.MetricID;
import org.eclipse.microprofile.metrics.MetricRegistry;

import graphql.schema.DataFetchingEnvironment;
import io.smallrye.metrics.MetricRegistries;

public class MetricDecorator implements DataFetcherDecorator {

    private Map<DataFetchingEnvironment, Long> startTimes = Collections.synchronizedMap(new IdentityHashMap<>());

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
            MetricRegistries.get(MetricRegistry.Type.VENDOR).simpleTimer(metricID.getName(), metricID.getTagsAsArray())
                    .update(Duration.ofNanos(duration));
        }

    }

}
