package io.smallrye.graphql.execution;

import graphql.execution.AsyncExecutionStrategy;
import io.smallrye.graphql.execution.error.ExceptionHandler;

/**
 * Execution strategy to use our own exception handler
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class QueryExecutionStrategy extends AsyncExecutionStrategy {

    public QueryExecutionStrategy(ExceptionHandler exceptionHandler) {
        super(exceptionHandler);
    }
}
