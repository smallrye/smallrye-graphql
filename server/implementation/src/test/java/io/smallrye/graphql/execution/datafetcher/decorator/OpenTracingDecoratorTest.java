package io.smallrye.graphql.execution.datafetcher.decorator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import graphql.GraphQLContext;
import graphql.schema.DataFetchingEnvironment;
import io.opentracing.mock.MockSpan;
import io.smallrye.graphql.execution.datafetcher.MockDataFetchEnvironment;
import io.smallrye.graphql.execution.datafetcher.MockExecutionContext;
import io.smallrye.graphql.opentracing.MockTracerOpenTracingService;

public class OpenTracingDecoratorTest {

    @BeforeEach
    public void reset() {
        MockTracerOpenTracingService.MOCK_TRACER.reset();
    }

    @Test
    public void testTracingWorks() throws Exception {
        OpenTracingDecorator decorator = new OpenTracingDecorator();

        DataFetchingEnvironment dfe = MockDataFetchEnvironment.myFastQueryDfe("Query", "myFastQuery", "someOperation", "1");

        MockExecutionContext mockExecutionContext = new MockExecutionContext();
        mockExecutionContext.setDataFetchingEnvironment(dfe);
        mockExecutionContext.setGraphQLContext(GraphQLContext.newContext().build());

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
        mockExecutionContext.setGraphQLContext(GraphQLContext.newContext().build());
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
        mockExecutionContext.setGraphQLContext(GraphQLContext.newContext().build());
        CompletableFuture<String> future = new CompletableFuture<>();
        CompletableFuture<String> asyncFutur = future.thenApplyAsync(Function.identity());//Let future complete in different Thread
        mockExecutionContext.setResult(asyncFutur);

        CompletableFuture<String> result = (CompletableFuture<String>) decorator.execute(mockExecutionContext);

        assertNoSpanIsFinished();

        future.complete("Result");
        assertEquals("Result", result.get());

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
        mockExecutionContext.setGraphQLContext(GraphQLContext.newContext().build());
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
    public void testExceptionallyWorks() {
        OpenTracingDecorator decorator = new OpenTracingDecorator();

        DataFetchingEnvironment dfe = MockDataFetchEnvironment.myFastQueryDfe("Query", "myFastQuery", "someOperation", "1");

        MockExecutionContext mockExecutionContext = new MockExecutionContext();
        mockExecutionContext.setDataFetchingEnvironment(dfe);
        mockExecutionContext.setGraphQLContext(GraphQLContext.newContext().build());
        mockExecutionContext.setResult(() -> {
            throw new RuntimeException();
        });
        try {
            decorator.execute(mockExecutionContext);
            fail();
        } catch (Exception expected) {
        }

        assertOneSpanIsFinished();
        MockSpan span = MockTracerOpenTracingService.MOCK_TRACER.finishedSpans().get(0);
        assertContainsGraphQLTags(span, "Query", "myFastQuery", "someOperation", "1");
        assertErrorIsLogged(span);
    }

    void assertNoSpanIsFinished() {
        assertEquals(0, MockTracerOpenTracingService.MOCK_TRACER.finishedSpans().size(),
                "No span should be finished before execution completes");
    }

    void assertOneSpanIsFinished() {
        assertEquals(1, MockTracerOpenTracingService.MOCK_TRACER.finishedSpans().size(),
                "One span should be finished after execution completes");
    }

    void assertContainsGraphQLTags(MockSpan span, String typeName, String fieldName, String operationName, String executionId) {
        assertEquals("GraphQL:" + typeName + "." + fieldName, span.operationName(),
                "Operation name should contain type and field");
        assertEquals(executionId, span.tags().get("graphql.executionId"), "ExecutionId should be present in span");
        assertEquals(fieldName, span.tags().get("graphql.field"), "field name should be present in span");
        assertEquals(typeName, span.tags().get("graphql.parent"), "parent name should be present in span");
        assertEquals("/" + typeName + "/" + fieldName, span.tags().get("graphql.path"), "path should be present in span");
        assertEquals(operationName, span.tags().get("graphql.operationName"), "operation name should be present in span");
    }

    void assertErrorIsLogged(MockSpan span) {
        final MockSpan.LogEntry logEntry = span.logEntries().get(0);
        assertEquals("error", logEntry.fields().get("event"));

    }

    void assertNoErrorLogged(MockSpan span) {
        for (final MockSpan.LogEntry logEntry : span.logEntries()) {
            assertNotEquals("error", logEntry.fields().get("event"));
        }
    }

}
