/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.smallrye.graphql.execution;

import java.util.concurrent.CompletableFuture;

import graphql.ExecutionResult;
import graphql.execution.AsyncExecutionStrategy;
import graphql.execution.ExecutionContext;
import graphql.execution.ExecutionStrategyParameters;
import graphql.execution.NonNullableFieldWasNullException;

/**
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class QueryExecutionStrategy extends AsyncExecutionStrategy {

    public QueryExecutionStrategy() {
        super(new ExceptionHandler());
    }

    @Override
    public CompletableFuture<ExecutionResult> execute(ExecutionContext executionContext, ExecutionStrategyParameters parameters)
            throws NonNullableFieldWasNullException {

        //executionContext.
        CompletableFuture<ExecutionResult> execute = super.execute(executionContext, parameters);

        return execute;
    }

}
