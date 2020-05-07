package io.smallrye.graphql.execution;

import graphql.ExecutionInput;
import graphql.ExecutionResult;

public interface ExecutionDecorator {
    default void before(ExecutionInput executionInput) {

    }

    default void after(ExecutionInput executionInput, ExecutionResult executionResult) {

    }

    default void onError(ExecutionInput executionInput, Throwable throwable) {

    }
}
