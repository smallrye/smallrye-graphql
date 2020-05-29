package io.smallrye.graphql.execution.datafetcher.decorator;

import static org.junit.Assert.*;

import java.util.concurrent.CompletableFuture;

import org.eclipse.microprofile.metrics.MetricRegistry;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import graphql.GraphQLContext;
import graphql.schema.DataFetchingEnvironment;
import io.smallrye.graphql.execution.datafetcher.MockDataFetchEnvironment;
import io.smallrye.graphql.execution.datafetcher.MockExecutionContext;
import io.smallrye.graphql.metrics.TestMetricsServiceImpl;
import io.smallrye.graphql.spi.MetricsService;

public class MetricDecoratorTest {

    @Before
    public void setUp() throws Exception {
        MetricsService service = MetricsService.load();
        service.getMetricRegistry(MetricRegistry.Type.VENDOR);
        TestMetricsServiceImpl.vendorRegistry.simpleTimeMetadatas.clear();
        TestMetricsServiceImpl.vendorRegistry.simpleTimers.clear();
    }

    @Test
    public void testSimpleTimerCountWorks() throws Exception {
        MetricDecorator decorator = new MetricDecorator();

        DataFetchingEnvironment dfe = myFastQueryDfe();

        MockExecutionContext mockExecutionContext = new MockExecutionContext();
        mockExecutionContext.setDataFetchingEnvironment(dfe);
        mockExecutionContext.setNewGraphQLContext(GraphQLContext.newContext().build());

        decorator.execute(mockExecutionContext);
        decorator.execute(mockExecutionContext);
        decorator.execute(mockExecutionContext);

        DataFetchingEnvironment dfe2 = myOtherQueryDfe();

        MockExecutionContext mockExecutionContext2 = new MockExecutionContext();
        mockExecutionContext2.setDataFetchingEnvironment(dfe2);
        mockExecutionContext2.setNewGraphQLContext(GraphQLContext.newContext().build());

        decorator.execute(mockExecutionContext2);
        decorator.execute(mockExecutionContext2);

        TestMetricsServiceImpl.MockMetricsRegistry registry = TestMetricsServiceImpl.vendorRegistry;
        assertEquals(2, registry.simpleTimers.size());
        assertEquals(3, registry.simpleTimers.get("mp_graphql_Query_myFastQuery").getCount());
        assertEquals(2, registry.simpleTimers.get("mp_graphql_Query_myOtherQuery").getCount());
    }

    @Test
    public void testAsyncWorks() throws Exception {
        MetricDecorator decorator = new MetricDecorator();
        DataFetchingEnvironment dfe = myFastQueryDfe();

        MockExecutionContext mockExecutionContext = new MockExecutionContext();
        mockExecutionContext.setDataFetchingEnvironment(dfe);
        mockExecutionContext.setNewGraphQLContext(GraphQLContext.newContext().build());
        final CompletableFuture<Object> result = new CompletableFuture<>();
        mockExecutionContext.setResult(result);

        decorator.execute(mockExecutionContext);

        TestMetricsServiceImpl.MockMetricsRegistry registry = TestMetricsServiceImpl.vendorRegistry;
        assertEquals(0, registry.simpleTimers.size());
        result.complete("");
        assertEquals(1, registry.simpleTimers.size());
        assertEquals(1, registry.simpleTimers.get("mp_graphql_Query_myFastQuery").getCount());
    }

    @Test
    public void testAsyncExceptionWorks() throws Exception {
        MetricDecorator decorator = new MetricDecorator();
        DataFetchingEnvironment dfe = myFastQueryDfe();

        MockExecutionContext mockExecutionContext = new MockExecutionContext();
        mockExecutionContext.setDataFetchingEnvironment(dfe);
        mockExecutionContext.setNewGraphQLContext(GraphQLContext.newContext().build());
        final CompletableFuture<Object> result = new CompletableFuture<>();
        mockExecutionContext.setResult(result);

        decorator.execute(mockExecutionContext);

        TestMetricsServiceImpl.MockMetricsRegistry registry = TestMetricsServiceImpl.vendorRegistry;
        assertEquals(0, registry.simpleTimers.size());
        result.completeExceptionally(new RuntimeException());
        assertEquals(1, registry.simpleTimers.size());
        assertEquals(1, registry.simpleTimers.get("mp_graphql_Query_myFastQuery").getCount());
    }

    @Test
    public void testExceptionallyWorks() throws Exception {
        MetricDecorator decorator = new MetricDecorator();

        DataFetchingEnvironment dfe = myFastQueryDfe();

        MockExecutionContext mockExecutionContext = new MockExecutionContext();
        mockExecutionContext.setDataFetchingEnvironment(dfe);
        mockExecutionContext.setNewGraphQLContext(GraphQLContext.newContext().build());
        mockExecutionContext.setResult(() -> {
            throw new RuntimeException();
        });
        try {
            decorator.execute(mockExecutionContext);
            Assert.fail();
        } catch (Exception expected) {
        }

        TestMetricsServiceImpl.MockMetricsRegistry registry = TestMetricsServiceImpl.vendorRegistry;
        assertEquals(1, registry.simpleTimers.size());
        assertEquals(1, registry.simpleTimers.get("mp_graphql_Query_myFastQuery").getCount());
    }

    DataFetchingEnvironment myFastQueryDfe() {
        return MockDataFetchEnvironment.myFastQueryDfe("Query", "myFastQuery", "someOperation", "1");
    }

    DataFetchingEnvironment myOtherQueryDfe() {
        return MockDataFetchEnvironment.myFastQueryDfe("Query", "myOtherQuery", "someOperation", "1");
    }

}
