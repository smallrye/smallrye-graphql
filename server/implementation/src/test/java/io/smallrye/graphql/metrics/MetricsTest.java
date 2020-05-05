package io.smallrye.graphql.metrics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Test;

import graphql.GraphQLContext;
import graphql.language.Field;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLNamedType;
import io.smallrye.graphql.bootstrap.Bootstrap;
import io.smallrye.graphql.execution.datafetcher.decorator.MetricDecorator;
import io.smallrye.graphql.metrics.TestMetricsServiceImpl.MockMetricsRegistry;
import io.smallrye.graphql.schema.model.Operation;
import io.smallrye.graphql.schema.model.OperationType;
import io.smallrye.graphql.schema.model.Schema;
import io.smallrye.graphql.spi.MetricsService;

public class MetricsTest {

    @Test
    public void testCanLoadMetricsService() throws Exception {
        MetricsService service = MetricsService.load();
        assertNotNull(service);
        assertTrue(service instanceof TestMetricsServiceImpl);
    }

    @Test
    public void testMetricsServiceRegisteredInBootstrap() throws Exception {
        Operation query = new Operation();
        query.setName("myQuery");
        query.setOperationType(OperationType.Query);
        Operation mutation = new Operation();
        mutation.setName("myMutation");
        mutation.setOperationType(OperationType.Mutation);

        Schema schema = new Schema();
        schema.setQueries(Collections.singleton(query));
        schema.setMutations(Collections.singleton(mutation));

        TestMetricsServiceImpl metricServiceImpl = new TestMetricsServiceImpl();
        MockMetricsRegistry metricRegistry = metricServiceImpl.vendorRegistry;
        Bootstrap.registerMetrics(schema, metricRegistry);

        assertEquals(2, metricServiceImpl.vendorRegistry.simpleTimeMetadatas.size());
        assertEquals("mp_graphql_Query_myQuery", metricServiceImpl.vendorRegistry.simpleTimeMetadatas.get(0).getDisplayName());
        assertEquals("mp_graphql_Mutation_myMutation",
                metricServiceImpl.vendorRegistry.simpleTimeMetadatas.get(1).getDisplayName());
    }

    @Test
    public void testSimpleTimerCountWorks() throws Exception {
        MetricDecorator decorator = new MetricDecorator();
        Field field = mock(Field.class);
        when(field.getName()).thenReturn("myFastQuery");
        GraphQLNamedType query = mock(GraphQLNamedType.class);
        when(query.getName()).thenReturn("Query");

        DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
        when(dfe.getParentType()).thenReturn(query);
        when(dfe.getField()).thenReturn(field);

        decorator.before(dfe);
        decorator.after(dfe, GraphQLContext.newContext().build());

        decorator.before(dfe);
        decorator.after(dfe, GraphQLContext.newContext().build());

        decorator.before(dfe);
        decorator.after(dfe, GraphQLContext.newContext().build());

        Field field2 = mock(Field.class);
        when(field2.getName()).thenReturn("myOtherQuery");
        DataFetchingEnvironment dfe2 = mock(DataFetchingEnvironment.class);
        when(dfe2.getField()).thenReturn(field2);
        when(dfe2.getParentType()).thenReturn(query);

        decorator.before(dfe2);
        decorator.after(dfe2, GraphQLContext.newContext().build());

        decorator.before(dfe2);
        decorator.after(dfe2, GraphQLContext.newContext().build());

        MockMetricsRegistry registry = TestMetricsServiceImpl.INSTANCE.vendorRegistry;
        assertEquals(2, registry.simpleTimers.size());
        assertEquals(3, registry.simpleTimers.get("mp_graphql_Query_myFastQuery").getCount());
        assertEquals(2, registry.simpleTimers.get("mp_graphql_Query_myOtherQuery").getCount());
    }
}
