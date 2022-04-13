package io.smallrye.graphql.cdi.tracing;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionException;

import javax.annotation.Priority;
import javax.enterprise.inject.spi.CDI;

import graphql.schema.DataFetchingEnvironment;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.smallrye.graphql.api.Context;
import io.smallrye.graphql.cdi.config.ConfigKey;
import io.smallrye.graphql.execution.event.Priorities;
import io.smallrye.graphql.spi.EventingService;

/**
 * Listening for event and create traces from it
 *
 * FIXME: currently, this places the work of all fetchers inside an operation (execution)
 * under the same parent, which is the execution itself. It would be cool
 * to define some reasonable hierarchy between fetchers, so
 * for example, when evaluating a source method requires evaluating another source method,
 * the second one would be a child of the first one.
 *
 * @author Jan Martiska (jmartisk@redhat.com)
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@Priority(Priorities.FIRST_IN_LAST_OUT)
public class TracingService implements EventingService {

    private final String SPAN_CLASS = "io.opentracing.Span";
    private final String SCOPE_CLASS = "io.opentracing.Scope";

    // key = execution ID
    // value = the root span of this execution
    private final Map<String, Span> spans = Collections.synchronizedMap(new IdentityHashMap<>());
    private final Map<String, Scope> scopes = Collections.synchronizedMap(new IdentityHashMap<>());

    private Tracer tracer;

    @Override
    public void beforeExecute(Context context) {
        // FIXME: if operationName is not set in the request explicitly, this is empty
        String operationName = getOperationName(context);

        Span span = getTracer().buildSpan(operationName)
                .asChildOf(getTracer().activeSpan())
                .withTag("graphql.executionId", context.getExecutionId())
                .withTag("graphql.operationType", getOperationNameString(context.getRequestedOperationTypes()))
                .withTag("graphql.operationName", context.getOperationName().orElse(EMPTY))
                .start();
        Scope scope = tracer.activateSpan(span);
        spans.put(context.getExecutionId(), span);
        scopes.put(context.getExecutionId(), scope);
    }

    @Override
    public void afterExecute(Context context) {
        Span span = spans.remove(context.getExecutionId());
        if (span != null) {
            scopes.get(context.getExecutionId()).close();
            span.finish();
        }
    }

    @Override
    public void errorExecute(String executionId, Throwable t) {
        Span span = spans.remove(executionId);
        if (span != null) {
            Map<String, Object> error = new HashMap<>();
            error.put("event.object", t);
            error.put("event", "error");
            span.log(error);
            span.finish();
        }
    }

    @Override
    public void beforeDataFetch(Context context) {
        Span parentSpan = getParentSpan(getTracer(), context);

        Span span = getTracer().buildSpan(getOperationNameForParentType(context))
                .asChildOf(parentSpan)
                .withTag("graphql.executionId", context.getExecutionId())
                .withTag("graphql.operationType", getOperationNameString(context.getOperationType()))
                .withTag("graphql.operationName", context.getOperationName().orElse(EMPTY))
                .withTag("graphql.parent", context.getParentTypeName().orElse(EMPTY))
                .withTag("graphql.field", context.getFieldName())
                .withTag("graphql.path", context.getPath())
                .start();
        Scope scope = tracer.activateSpan(span);

        DataFetchingEnvironment dfe = context.unwrap(DataFetchingEnvironment.class);
        dfe.getGraphQlContext().put(SPAN_CLASS, parentSpan);
        dfe.getGraphQlContext().put(SCOPE_CLASS, scope);
    }

    // FIXME: is the fetcher is asynchronous, this typically ends its span before
    // the work is actually done - after the fetcher itself returns a future.
    // We currently don't have a way to find out when
    // the right moment to close this span is
    @Override
    public void afterDataFetch(Context context) {
        Span span = tracer.activeSpan();
        DataFetchingEnvironment dfe = context.unwrap(DataFetchingEnvironment.class);
        Scope scope = dfe.getGraphQlContext().get(SCOPE_CLASS);
        scope.close();
        span.finish();
    }

    @Override
    public void errorDataFetch(String executionId, Throwable t) {
        Span span = spans.get(executionId);
        if (span != null) {
            logError(span, t);
        }
    }

    @Override
    public String getConfigKey() {
        return ConfigKey.ENABLE_TRACING;
    }

    private Tracer getTracer() {
        if (tracer == null) {
            this.tracer = CDI.current().select(Tracer.class).get();
        }
        return tracer;
    }

    private Span getParentSpan(Tracer tracer, final Context context) {
        DataFetchingEnvironment dfe = context.unwrap(DataFetchingEnvironment.class);
        if (dfe.getGraphQlContext().hasKey(SPAN_CLASS)) {
            return dfe.getGraphQlContext().get(SPAN_CLASS);
        }
        return tracer.activeSpan();
    }

    private void logError(Span span, Throwable throwable) {
        if (throwable instanceof InvocationTargetException || throwable instanceof CompletionException) {
            // Unwrap to get real exception
            throwable = throwable.getCause();
        }

        Map<String, Object> error = new HashMap<>();
        if (throwable != null) {
            error.put("error.object", throwable.getMessage());
        }
        error.put("event", "error");
        span.log(error);
        span.setTag("error", true);
    }

    private String getOperationNameForParentType(Context context) {
        String parent = context.getParentTypeName().orElse(EMPTY);
        String name = PREFIX + ":" + parent + "." + context.getFieldName();
        return name;
    }

    private static String getOperationName(Context context) {
        if (context.getOperationName().isPresent()) {
            return PREFIX + ":" + context.getOperationName().get();
        }
        return PREFIX;
    }

    private String getOperationNameString(List<String> types) {
        return String.join(UNDERSCORE, types);
    }

    private String getOperationNameString(String... types) {
        return getOperationNameString(Arrays.asList(types));
    }

    private static final String UNDERSCORE = "_";
    private static final String EMPTY = "";
    private static final String PREFIX = "GraphQL";
}
