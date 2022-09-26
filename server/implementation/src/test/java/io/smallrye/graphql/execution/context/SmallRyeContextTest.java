package io.smallrye.graphql.execution.context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import graphql.ExecutionResult;

class SmallRyeContextTest {

    @Test
    void unwrapExecutionResult() {
        SmallRyeContext context = new SmallRyeContext(SmallRyeContextManager.class.getName());
        assertNull(context.unwrap(ExecutionResult.class));

        ExecutionResult executionResult = mock(ExecutionResult.class);
        context.setExecutionResult(executionResult);
        assertEquals(context.unwrap(ExecutionResult.class), executionResult);
    }
}
