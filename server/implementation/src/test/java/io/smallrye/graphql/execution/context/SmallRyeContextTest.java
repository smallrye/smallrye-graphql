package io.smallrye.graphql.execution.context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import graphql.ExecutionResult;

import java.util.Optional;

import org.junit.jupiter.api.Test;

class SmallRyeContextTest {

    @Test
    void getAndSetExecutionResult() {
        SmallRyeContext context = new SmallRyeContext(SmallRyeContextManager.class.getName());
        assertEquals(context.getExecutionResult(), Optional.empty());

        ExecutionResult executionResult = mock(ExecutionResult.class);
        context.setExecutionResult(executionResult);
        assertEquals(context.getExecutionResult(), Optional.of(executionResult));
    }
}