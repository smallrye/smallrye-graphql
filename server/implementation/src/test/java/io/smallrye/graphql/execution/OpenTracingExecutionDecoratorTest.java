package io.smallrye.graphql.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQLContext;
import graphql.execution.ExecutionId;
import io.opentracing.mock.MockSpan;
import io.smallrye.graphql.opentracing.MockTracerOpenTracingService;

public class OpenTracingExecutionDecoratorTest {

    @BeforeEach
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

        assertEquals(1, MockTracerOpenTracingService.MOCK_TRACER.finishedSpans().size(), "One span should be finished");
        MockSpan span = MockTracerOpenTracingService.MOCK_TRACER.finishedSpans().get(0);
        assertEquals("GraphQL", span.operationName());
        assertEquals("1", span.tags().get("graphql.executionId"), "ExecutionId should be present in span");
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

        assertEquals(1, MockTracerOpenTracingService.MOCK_TRACER.finishedSpans().size(), "One span should be finished");
        MockSpan span = MockTracerOpenTracingService.MOCK_TRACER.finishedSpans().get(0);
        assertEquals("GraphQL:someOperation", span.operationName());
        assertEquals("someOperation", span.tags().get("graphql.operationName"),
                "operation name should be present in span");
    }

}
