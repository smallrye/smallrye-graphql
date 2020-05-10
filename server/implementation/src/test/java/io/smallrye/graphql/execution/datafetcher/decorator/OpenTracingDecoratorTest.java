package io.smallrye.graphql.execution.datafetcher.decorator;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import graphql.GraphQLContext;
import graphql.schema.DataFetchingEnvironment;
import io.opentracing.mock.MockSpan;
import io.smallrye.graphql.execution.datafetcher.MockDataFetchEnvironment;
import io.smallrye.graphql.execution.datafetcher.MockExecutionContext;
import io.smallrye.graphql.opentracing.MockTracerOpenTracingService;

public class OpenTracingDecoratorTest {

    @Before
    public void reset() {
        MockTracerOpenTracingService.MOCK_TRACER.reset();
    }

    @Test
    public void testTracingWorks() throws Exception {
        OpenTracingDecorator decorator = new OpenTracingDecorator();

        DataFetchingEnvironment dfe = MockDataFetchEnvironment.myFastQueryDfe("Query", "myFastQuery", "someOperation", "1");

        MockExecutionContext mockExecutionContext = new MockExecutionContext();
        mockExecutionContext.setDataFetchingEnvironment(dfe);
        mockExecutionContext.setNewGraphQLContext(GraphQLContext.newContext().build());

        decorator.execute(mockExecutionContext);

        assertOneSpanIsFinished();

        MockSpan span = MockTracerOpenTracingService.MOCK_TRACER.finishedSpans().get(0);
        assertContainsGraphQLTags(span, "Query", "myFastQuery", "someOperation", "1");
        assertNoErrorLogged(span);
    }

    @Test
    public void testAsyncWorks() throws Exception {
        OpenTracingDecorator decorator = new OpenTracingDecorator();
        DataFetchingEnvironment dfe = MockDataFetchEnvironment.myFastQueryDfe("Query", "myFastQuery", "someOperation", "1");

        MockExecutionContext mockExecutionContext = new MockExecutionContext();
        mockExecutionContext.setDataFetchingEnvironment(dfe);
        mockExecutionContext.setNewGraphQLContext(GraphQLContext.newContext().build());
        final CompletableFuture<Object> result = new CompletableFuture<>();
        mockExecutionContext.setResult(result);

        decorator.execute(mockExecutionContext);

        assertNoSpanIsFinished();

        result.complete("");

        assertOneSpanIsFinished();

        MockSpan span = MockTracerOpenTracingService.MOCK_TRACER.finishedSpans().get(0);
        assertContainsGraphQLTags(span, "Query", "myFastQuery", "someOperation", "1");
        assertNoErrorLogged(span);
    }

    @Test
    public void testSpanOverMultipleThreadsWorks() throws Exception {
        OpenTracingDecorator decorator = new OpenTracingDecorator();
        DataFetchingEnvironment dfe = MockDataFetchEnvironment.myFastQueryDfe("Query", "myFastQuery", "someOperation", "1");

        MockExecutionContext mockExecutionContext = new MockExecutionContext();
        mockExecutionContext.setDataFetchingEnvironment(dfe);
        mockExecutionContext.setNewGraphQLContext(GraphQLContext.newContext().build());
        CompletableFuture<String> future = new CompletableFuture<>();
        CompletableFuture<String> asyncFutur = future.thenApplyAsync(Function.identity());//Let future complete in different Thread
        mockExecutionContext.setResult(asyncFutur);

        CompletableFuture<String> result = (CompletableFuture<String>) decorator.execute(mockExecutionContext);

        assertNoSpanIsFinished();

        future.complete("Result");
        Assert.assertEquals("Result", result.get());

        assertOneSpanIsFinished();

        MockSpan span = MockTracerOpenTracingService.MOCK_TRACER.finishedSpans().get(0);
        assertContainsGraphQLTags(span, "Query", "myFastQuery", "someOperation", "1");
        assertNoErrorLogged(span);
    }

    @Test
    public void testAsyncExceptionWorks() throws Exception {
        OpenTracingDecorator decorator = new OpenTracingDecorator();
        DataFetchingEnvironment dfe = MockDataFetchEnvironment.myFastQueryDfe("Query", "myFastQuery", "someOperation", "1");

        MockExecutionContext mockExecutionContext = new MockExecutionContext();
        mockExecutionContext.setDataFetchingEnvironment(dfe);
        mockExecutionContext.setNewGraphQLContext(GraphQLContext.newContext().build());
        final CompletableFuture<Object> result = new CompletableFuture<>();
        mockExecutionContext.setResult(result);

        decorator.execute(mockExecutionContext);

        assertNoSpanIsFinished();

        result.completeExceptionally(new RuntimeException());

        assertOneSpanIsFinished();
        MockSpan span = MockTracerOpenTracingService.MOCK_TRACER.finishedSpans().get(0);
        assertContainsGraphQLTags(span, "Query", "myFastQuery", "someOperation", "1");
        assertErrorIsLogged(span);
    }

    @Test
    public void testExceptionallyWorks() throws Exception {
        OpenTracingDecorator decorator = new OpenTracingDecorator();

        DataFetchingEnvironment dfe = MockDataFetchEnvironment.myFastQueryDfe("Query", "myFastQuery", "someOperation", "1");

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

        assertOneSpanIsFinished();
        MockSpan span = MockTracerOpenTracingService.MOCK_TRACER.finishedSpans().get(0);
        assertContainsGraphQLTags(span, "Query", "myFastQuery", "someOperation", "1");
        assertErrorIsLogged(span);
    }

    void assertNoSpanIsFinished() {
        Assert.assertEquals("No span should be finished before execution completes", 0,
                MockTracerOpenTracingService.MOCK_TRACER.finishedSpans().size());
    }

    void assertOneSpanIsFinished() {
        Assert.assertEquals("One span should be finished after execution completes", 1,
                MockTracerOpenTracingService.MOCK_TRACER.finishedSpans().size());
    }

    void assertContainsGraphQLTags(MockSpan span, String typeName, String fieldName, String operationName, String executionId) {
        Assert.assertEquals("Operation name should contain type and field",
                "GraphQL:" + typeName + "." + fieldName, span.operationName());
        Assert.assertEquals("ExecutionId should be present in span", executionId, span.tags().get("graphql.executionId"));
        Assert.assertEquals("field name should be present in span", fieldName, span.tags().get("graphql.field"));
        Assert.assertEquals("parent name should be present in span", typeName, span.tags().get("graphql.parent"));
        Assert.assertEquals("path should be present in span",
                "/" + typeName + "/" + fieldName, span.tags().get("graphql.path"));
        Assert.assertEquals("operation name should be present in span", operationName,
                span.tags().get("graphql.operationName"));
    }

    void assertErrorIsLogged(MockSpan span) {
        final MockSpan.LogEntry logEntry = span.logEntries().get(0);
        Assert.assertEquals("error", logEntry.fields().get("event"));

    }

    void assertNoErrorLogged(MockSpan span) {
        for (final MockSpan.LogEntry logEntry : span.logEntries()) {
            Assert.assertNotEquals("error", logEntry.fields().get("event"));
        }
    }

}
