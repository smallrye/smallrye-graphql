package io.smallrye.graphql.execution.datafetcher.decorator;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;

import graphql.GraphQLContext;
import graphql.schema.DataFetchingEnvironment;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.smallrye.graphql.execution.datafetcher.helper.NameHelper;
import io.smallrye.graphql.spi.OpenTracingService;

public class OpenTracingDecorator implements DataFetcherDecorator {
    private static final Object PARENT_SPAN_KEY = Span.class;

    private final Map<DataFetchingEnvironment, Scope> scopes = Collections.synchronizedMap(new IdentityHashMap<>());

    OpenTracingService openTracingService = OpenTracingService.load();

    @Override
    public void before(final DataFetchingEnvironment env) {
        Tracer tracer = openTracingService.getTracer();

        String parent = NameHelper.getName(env.getParentType());

        String name = "GraphQL:" + parent + "." + env.getField().getName();

        Span parentSpan = getParentSpan(tracer, env);

        Scope scope = tracer.buildSpan(name)
                .asChildOf(parentSpan)
                .withTag("graphql.executionId", env.getExecutionId().toString())
                .withTag("graphql.operationName", env.getOperationDefinition().getName())
                .withTag("graphql.parent", parent)
                .withTag("graphql.field", env.getField().getName())
                .withTag("graphql.path", env.getExecutionStepInfo().getPath().toString())
                .startActive(true);

        scopes.put(env, scope);
    }

    private Span getParentSpan(Tracer tracer, final DataFetchingEnvironment env) {
        final GraphQLContext localContext = env.getLocalContext();
        if (localContext != null && localContext.hasKey(PARENT_SPAN_KEY)) {
            return localContext.get(PARENT_SPAN_KEY);
        }

        final GraphQLContext rootContext = env.getContext();
        if (rootContext != null && rootContext.hasKey(PARENT_SPAN_KEY)) {
            return rootContext.get(PARENT_SPAN_KEY);
        }

        return tracer.activeSpan();
    }

    @Override
    public void after(final DataFetchingEnvironment env, GraphQLContext newGraphQLContext) {
        Scope scope = scopes.remove(env);
        if (scope != null) {
            scope.close();
            newGraphQLContext.put(PARENT_SPAN_KEY, scope.span());
        }
    }

}
