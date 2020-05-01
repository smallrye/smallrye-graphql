package io.smallrye.graphql.execution.datafetcher.decorator;

import java.time.Duration;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;

import org.eclipse.microprofile.metrics.MetricID;
import org.eclipse.microprofile.metrics.MetricRegistry;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNamedType;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLType;
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
            MetricID metricID = new MetricID("mp_graphql_" + getName(dfe.getParentType()) + "_" + dfe.getField().getName());
            metricRegistry.simpleTimer(metricID.getName(), metricID.getTagsAsArray())
                    .update(Duration.ofNanos(duration));
        }
    }

    private String getName(GraphQLType graphQLType) {
        if (graphQLType instanceof GraphQLNamedType) {
            return ((GraphQLNamedType) graphQLType).getName();
        } else if (graphQLType instanceof GraphQLNonNull) {
            return getName(((GraphQLNonNull) graphQLType).getWrappedType());
        } else if (graphQLType instanceof GraphQLList) {
            return getName(((GraphQLList) graphQLType).getWrappedType());
        }
        return "";
    }

}
