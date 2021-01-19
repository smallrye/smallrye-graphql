package io.smallrye.graphql.cdi.tracing;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionException;

import javax.enterprise.inject.spi.CDI;

import graphql.ExecutionInput;
import graphql.GraphQLContext;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNamedType;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLType;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.smallrye.graphql.api.Context;
import io.smallrye.graphql.cdi.config.ConfigKey;
import io.smallrye.graphql.spi.EventingService;

/**
 * Listening for event and create traces from it
 *
 * @author Jan Martiska (jmartisk@redhat.com)
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class TracingService implements EventingService {

    private final String SPAN_CLASS = "io.opentracing.Span";
    private final Map<String, Span> spans = Collections.synchronizedMap(new IdentityHashMap<>());

    private Tracer tracer;

    @Override
    public void beforeExecute(Context context) {
        ExecutionInput executionInput = context.unwrap(ExecutionInput.class);
        String operationName = getOperationName(executionInput);

        Span span = getTracer().buildSpan(operationName)
                .asChildOf(getTracer().activeSpan())
                .withTag("graphql.executionId", context.getExecutionId())
                .withTag("graphql.operationType", getOperationNameString(context.getRequestedOperationTypes()))
                .withTag("graphql.operationName", context.getOperationName().orElse(EMPTY))
                .start();
        tracer.activateSpan(span);

        ((GraphQLContext) executionInput.getContext()).put(SPAN_CLASS, span); // TODO: Do we need this ?
    }

    @Override
    public void beforeDataFetch(Context context) {
        final DataFetchingEnvironment env = context.unwrap(DataFetchingEnvironment.class);

        Span parentSpan = getParentSpan(getTracer(), env);

        Span span = getTracer().buildSpan(getOperationName(env))
                .asChildOf(parentSpan)
                .withTag("graphql.executionId", context.getExecutionId())
                .withTag("graphql.operationType", getOperationNameString(context.getOperationType()))
                .withTag("graphql.operationName", context.getOperationName().orElse(EMPTY))
                .withTag("graphql.parent", context.getParentTypeName().orElse(EMPTY))
                .withTag("graphql.field", context.getFieldName())
                .withTag("graphql.path", context.getPath())
                .start();
        tracer.activateSpan(span);

        GraphQLContext graphQLContext = env.getContext();
        graphQLContext.put(SPAN_CLASS, span);

        spans.put(context.getExecutionId(), span);
    }

    @Override
    public void errorDataFetch(String executionId, Throwable t) {
        Span span = spans.get(executionId);
        if (span != null) {
            logError(span, t);
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
        }
    }

    @Override
    public void afterDataFetch(Context context) {
        Span span = spans.remove(context.getExecutionId());
        span.finish();
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

    private Span getParentSpan(Tracer tracer, final DataFetchingEnvironment env) {
        final GraphQLContext localContext = env.getLocalContext();
        if (localContext != null && localContext.hasKey(Span.class)) {
            return localContext.get(Span.class);
        }

        final GraphQLContext rootContext = env.getContext();
        if (rootContext != null && rootContext.hasKey(Span.class)) {
            return rootContext.get(Span.class);
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

    private String getOperationName(final DataFetchingEnvironment env) {
        String parent = getName(env.getParentType());

        String name = PREFIX + ":" + parent + "." + env.getField().getName();

        return name;
    }

    private static String getOperationName(ExecutionInput executionInput) {
        if (executionInput.getOperationName() != null && !executionInput.getOperationName().isEmpty()) {
            return PREFIX + ":" + executionInput.getOperationName();
        }
        return PREFIX;
    }

    private String getName(GraphQLType graphQLType) {
        if (graphQLType instanceof GraphQLNamedType) {
            return ((GraphQLNamedType) graphQLType).getName();
        } else if (graphQLType instanceof GraphQLNonNull) {
            return getName(((GraphQLNonNull) graphQLType).getWrappedType());
        } else if (graphQLType instanceof GraphQLList) {
            return getName(((GraphQLList) graphQLType).getWrappedType());
        }
        return EMPTY;
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
