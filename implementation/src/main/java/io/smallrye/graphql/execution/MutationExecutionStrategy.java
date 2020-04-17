package io.smallrye.graphql.execution;

import graphql.execution.AsyncSerialExecutionStrategy;
import io.smallrye.graphql.execution.error.ExceptionHandler;

/**
 * Execution strategy to use our own exception handler
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class MutationExecutionStrategy extends AsyncSerialExecutionStrategy {

    public MutationExecutionStrategy(ExceptionHandler exceptionHandler) {
        super(exceptionHandler);
    }
}
