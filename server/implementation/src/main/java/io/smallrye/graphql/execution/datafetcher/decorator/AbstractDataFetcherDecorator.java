package io.smallrye.graphql.execution.datafetcher.decorator;

import java.util.concurrent.CompletionStage;

import io.smallrye.graphql.execution.datafetcher.ExecutionContext;

/**
 * Abstract implementation of {@link DataFetcherDecorator} which provides a simpler interface for implementations.
 */
public class AbstractDataFetcherDecorator implements DataFetcherDecorator {

    /**
     * Called if an error happened.
     * <p>
     * May be executed in a different thread than {@link #before} and {@link #after}.
     *
     * @param executionContext the current executionContext
     * @param throwable the error
     */
    protected void onError(final ExecutionContext executionContext, Throwable throwable) {

    }

    /**
     * Called if the executions completes normally.
     * <p>
     * May be executed in a different thread than {@link #before} and {@link #after}.
     *
     * @param executionContext the current executionContext
     * @param result the result
     */
    protected void onComplete(final ExecutionContext executionContext, Object result) {

    }

    /**
     * Called before the execution.
     *
     * @param executionContext the current executionContext
     */
    protected void before(final ExecutionContext executionContext) {

    }

    /**
     * Called after the execution.
     * <p>
     * May be executed in a different thread than {@link #before}, {@link #onComplete} and {@link #onError}.
     *
     * @param executionContext the current executionContext
     */
    protected void after(final ExecutionContext executionContext) {

    }

    @Override
    public Object execute(final ExecutionContext executionContext) throws Exception {
        before(executionContext);

        try {
            Object result = executionContext.proceed();
            if (result instanceof CompletionStage) {
                //completes async
                result = ((CompletionStage<?>) result).whenComplete((o, throwable) -> {
                    if (throwable != null) {
                        onError(executionContext, throwable);
                    } else {
                        onComplete(executionContext, o);
                    }
                    after(executionContext);
                });
            } else {
                onComplete(executionContext, result);
                after(executionContext);
            }
            return result;
        } catch (Exception e) {
            onError(executionContext, e);
            after(executionContext);
            throw e;
        }
    }

}
