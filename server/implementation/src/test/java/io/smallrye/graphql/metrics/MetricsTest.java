package io.smallrye.graphql.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import io.smallrye.graphql.bootstrap.Bootstrap;
import io.smallrye.graphql.metrics.TestMetricsServiceImpl.MockMetricsRegistry;
import io.smallrye.graphql.schema.model.Operation;
import io.smallrye.graphql.schema.model.OperationType;
import io.smallrye.graphql.schema.model.Schema;
import io.smallrye.graphql.spi.MetricsService;

public class MetricsTest {

    @Test
    public void testCanLoadMetricsService() {
        MetricsService service = MetricsService.load();
        assertNotNull(service);
        assertTrue(service instanceof TestMetricsServiceImpl);
    }

    @Test
    public void testMetricsServiceRegisteredInBootstrap() {
        Operation query = new Operation();
        query.setName("myQuery");
        query.setOperationType(OperationType.Query);
        Operation mutation = new Operation();
        mutation.setName("myMutation");
        mutation.setOperationType(OperationType.Mutation);

        Schema schema = new Schema();
        schema.setQueries(Collections.singleton(query));
        schema.setMutations(Collections.singleton(mutation));

        MockMetricsRegistry metricRegistry = TestMetricsServiceImpl.vendorRegistry;
        Bootstrap.registerMetrics(schema, metricRegistry);

        assertEquals(2, TestMetricsServiceImpl.vendorRegistry.simpleTimeMetadatas.size());
        assertEquals("mp_graphql_Query_myQuery",
                TestMetricsServiceImpl.vendorRegistry.simpleTimeMetadatas.get(0).getDisplayName());
        assertEquals("mp_graphql_Mutation_myMutation",
                TestMetricsServiceImpl.vendorRegistry.simpleTimeMetadatas.get(1).getDisplayName());
    }

}
