package io.smallrye.graphql.cdi.event.metrics;

import java.time.Duration;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.eclipse.microprofile.metrics.Metadata;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.MetricType;
import org.eclipse.microprofile.metrics.annotation.RegistryType;

import io.smallrye.graphql.api.Context;
import io.smallrye.graphql.cdi.config.GraphQLConfig;
import io.smallrye.graphql.cdi.event.annotation.AfterDataFetch;
import io.smallrye.graphql.cdi.event.annotation.BeforeDataFetch;
import io.smallrye.graphql.schema.model.Operation;

/**
 * Listening for event and create metrics from it
 * 
 * @author Jan Martiska (jmartisk@redhat.com)
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@ApplicationScoped
public class MetricsEventListener {

    @Inject
    @RegistryType(type = MetricRegistry.Type.VENDOR)
    MetricRegistry metricRegistry;

    @Inject
    GraphQLConfig graphQLConfig;

    private final Map<Context, Long> startTimes = Collections.synchronizedMap(new IdentityHashMap<>());

    public void createOperation(@Observes Operation operation) {
        if (graphQLConfig.isMetricsEnabled()) {
            final String name = getName(operation);
            final String description = getDescription(operation);

            Metadata metadata = Metadata.builder()
                    .withName(name)
                    .withType(MetricType.SIMPLE_TIMER)
                    .withDescription(description)
                    .build();
            metricRegistry.simpleTimer(metadata);
        }
    }

    public void beforeDataLoad(@Observes @BeforeDataFetch Context context) {
        if (graphQLConfig.isMetricsEnabled()) {
            startTimes.put(context, System.nanoTime());
        }
    }

    public void afterDataLoad(@Observes @AfterDataFetch Context context) {
        if (graphQLConfig.isMetricsEnabled()) {
            Long startTime = startTimes.remove(context);
            if (startTime != null) {
                long duration = System.nanoTime() - startTime;
                metricRegistry.simpleTimer(getName(context))
                        .update(Duration.ofNanos(duration));
            }
        }
    }

    private String getName(Context context) {
        return PRE + context.getOperationType().toString() + UNDERSCORE + context.getFieldName();
    }

    private String getName(Operation operation) {
        return PRE + operation.getOperationType().toString() + UNDERSCORE + operation.getName();
    }

    private String getDescription(Operation operation) {
        return "Call statistics for the "
                + operation.getOperationType().toString().toLowerCase()
                + " '"
                + operation.getName()
                + "'";
    }

    private static final String PRE = "mp_graphql_";
    private static final String UNDERSCORE = "_";
}
