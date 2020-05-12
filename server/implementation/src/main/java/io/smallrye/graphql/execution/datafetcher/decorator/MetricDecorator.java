package io.smallrye.graphql.execution.datafetcher.decorator;

import java.time.Duration;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;

import org.eclipse.microprofile.metrics.MetricRegistry;

import graphql.language.Field;
import graphql.schema.GraphQLType;
import io.smallrye.graphql.execution.MetricNaming;
import io.smallrye.graphql.execution.datafetcher.ExecutionContext;
import io.smallrye.graphql.spi.MetricsService;

/**
 * Measures data-fetcher executions.
 *
 * TODO: Count failures?
 */
public class MetricDecorator extends AbstractDataFetcherDecorator {

    private final Map<ExecutionContext, Long> startTimes = Collections.synchronizedMap(new IdentityHashMap<>());

    private final MetricRegistry metricRegistry;

    public MetricDecorator() {
        metricRegistry = MetricsService.load().getMetricRegistry(MetricRegistry.Type.VENDOR);
    }

    @Override
    public void before(ExecutionContext executionContext) {
        startTimes.put(executionContext, System.nanoTime());
    }

    @Override
    public void after(ExecutionContext executionContext) {
        GraphQLType parent = executionContext.dataFetchingEnvironment().getParentType();
        Field field = executionContext.dataFetchingEnvironment().getField();

        Long startTime = startTimes.remove(executionContext);
        if (startTime != null) {
            long duration = System.nanoTime() - startTime;
            String name = MetricNaming.fromTypeAndName(parent, field.getName());
            metricRegistry.simpleTimer(name).update(Duration.ofNanos(duration));
        }
    }

}
