package io.smallrye.graphql.execution;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQLContext;
import graphql.execution.ExecutionId;
import io.opentracing.mock.MockSpan;
import io.smallrye.graphql.opentracing.MockTracerOpenTracingService;

public class OpenTracingExecutionDecoratorTest {

    @Before
    public void reset() {
        MockTracerOpenTracingService.MOCK_TRACER.reset();
    }

    @Test
    public void testExecutionTracing() {
        ExecutionInput executionInput = ExecutionInput.newExecutionInput()
                .query("{}")
                .context(GraphQLContext.newContext())
                .executionId(ExecutionId.from("1"))
                .build();

        ExecutionResult executionResult = Mockito.mock(ExecutionResult.class);

        OpenTracingExecutionDecorator openTracingExecutionDecorator = new OpenTracingExecutionDecorator();

        openTracingExecutionDecorator.before(executionInput);
        openTracingExecutionDecorator.after(executionInput, executionResult);

        Assert.assertEquals("One span should be finished", 1, MockTracerOpenTracingService.MOCK_TRACER.finishedSpans().size());
        MockSpan span = MockTracerOpenTracingService.MOCK_TRACER.finishedSpans().get(0);
        Assert.assertEquals("GraphQL", span.operationName());
        Assert.assertEquals("ExecutionId should be present in span", "1", span.tags().get("graphql.executionId"));
    }

    @Test
    public void spanOperationNameShouldContainGraphQLOperationName() {
        ExecutionInput executionInput = ExecutionInput.newExecutionInput()
                .query("{}")
                .context(GraphQLContext.newContext())
                .operationName("someOperation")
                .executionId(ExecutionId.from("1"))
                .build();

        ExecutionResult executionResult = Mockito.mock(ExecutionResult.class);

        OpenTracingExecutionDecorator openTracingExecutionDecorator = new OpenTracingExecutionDecorator();

        openTracingExecutionDecorator.before(executionInput);
        openTracingExecutionDecorator.after(executionInput, executionResult);

        Assert.assertEquals("One span should be finished", 1, MockTracerOpenTracingService.MOCK_TRACER.finishedSpans().size());
        MockSpan span = MockTracerOpenTracingService.MOCK_TRACER.finishedSpans().get(0);
        Assert.assertEquals("GraphQL:someOperation", span.operationName());
        Assert.assertEquals("operation name should be present in span", "someOperation",
                span.tags().get("graphql.operationName"));
    }

}
