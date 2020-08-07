package io.smallrye.graphql.execution.datafetcher.decorator;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.CompletionException;

import graphql.GraphQLContext;
import graphql.schema.DataFetchingEnvironment;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.smallrye.graphql.execution.SpanNaming;
import io.smallrye.graphql.execution.datafetcher.ExecutionContext;
import io.smallrye.graphql.execution.datafetcher.helper.NameHelper;
import io.smallrye.graphql.spi.OpenTracingService;

/**
 * Traces data-fetcher executions.
 */
public class OpenTracingDecorator extends AbstractDataFetcherDecorator {

    private static final Object PARENT_SPAN_KEY = Span.class;

    OpenTracingService openTracingService = OpenTracingService.load();

    private final Map<ExecutionContext, Span> spans = Collections.synchronizedMap(new IdentityHashMap<>());

    @Override
    protected void onError(final ExecutionContext executionContext, final Throwable throwable) {
        Span span = spans.get(executionContext);
        logError(span, throwable);
    }

    @Override
    protected void before(final ExecutionContext executionContext) {
        final DataFetchingEnvironment env = executionContext.dataFetchingEnvironment();
        Tracer tracer = openTracingService.getTracer();

        Span parentSpan = getParentSpan(tracer, env);

        Scope scope = tracer.buildSpan(SpanNaming.getOperationName(env))
                .asChildOf(parentSpan)
                .withTag("graphql.executionId", env.getExecutionId().toString())
                .withTag("graphql.operationName", env.getOperationDefinition().getName())
                .withTag("graphql.parent", NameHelper.getName(env.getParentType()))
                .withTag("graphql.field", env.getField().getName())
                .withTag("graphql.path", env.getExecutionStepInfo().getPath().toString())
                .startActive(false);

        final Span span = scope.span();
        executionContext.graphQLContext().put(PARENT_SPAN_KEY, span);

        spans.put(executionContext, span);
    }

    @Override
    protected void after(final ExecutionContext executionContext) {
        Span span = spans.remove(executionContext);
        span.finish();
    }

    /**
     * @implNote The current scope must be closed in the thread in which it was opened,
     *           after work in the current thread has ended.
     *           So you can't use {@code before} because it is executed before the work in the current thread is finished,
     *           but you can't use {@code after} either because it can run in another thread.
     */
    @Override
    public Object execute(final ExecutionContext executionContext) throws Exception {
        try {
            return super.execute(executionContext);
        } finally {
            Tracer tracer = openTracingService.getTracer();
            tracer.scopeManager().active().close();
        }
    }

    private void logError(Span span, Throwable throwable) {
        if (throwable instanceof InvocationTargetException || throwable instanceof CompletionException) {
            // Unwrap to get real exception
            throwable = throwable.getCause();
        }

        Map<String, Object> error = new HashMap<>();
        error.put("error.object", throwable.getMessage());
        error.put("event", "error");
        span.log(error);
        span.setTag("error", true);
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

}
