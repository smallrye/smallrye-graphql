package io.smallrye.graphql.execution;

import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQLContext;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.smallrye.graphql.spi.OpenTracingService;

public class OpenTracingExecutionDecorator implements ExecutionDecorator {

    private final Map<ExecutionInput, Scope> executionScopes = Collections.synchronizedMap(new IdentityHashMap<>());

    OpenTracingService openTracingService = OpenTracingService.load();

    public OpenTracingExecutionDecorator() {
    }

    @Override
    public void before(final ExecutionInput executionInput) {
        Tracer tracer = openTracingService.getTracer();

        String operationName = "GraphQL";
        if (executionInput.getOperationName() != null && !executionInput.getOperationName().isEmpty()) {
            operationName = "GraphQL:" + executionInput.getOperationName();
        }

        Scope scope = tracer.buildSpan(operationName)
                .asChildOf(tracer.activeSpan())
                .withTag("graphql.executionId", executionInput.getExecutionId().toString())
                .withTag("graphql.operationName", executionInput.getOperationName())
                .startActive(true);

        executionScopes.put(executionInput, scope);

        ((GraphQLContext) executionInput.getContext()).put(Span.class, scope.span());
    }

    @Override
    public void after(final ExecutionInput executionInput, final ExecutionResult executionResult) {
        Scope scope = executionScopes.remove(executionInput);
        if (scope != null) {
            scope.close();
        }
    }

    @Override
    public void onError(final ExecutionInput executionInput, final Throwable throwable) {
        Scope scope = executionScopes.remove(executionInput);
        if (scope != null) {
            Map<String, Object> error = new HashMap<>();
            error.put("event.object", throwable);
            error.put("event", "error");
            scope.span().log(error);
            scope.close();
        }
    }

}
