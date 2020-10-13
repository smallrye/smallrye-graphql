package io.smallrye.graphql.cdi.tracing;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
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
import io.opentracing.Scope;
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

    private final Map<String, Span> spans = Collections.synchronizedMap(new IdentityHashMap<>());
    private final Map<String, Scope> scopes = Collections.synchronizedMap(new IdentityHashMap<>());

    private Tracer tracer;

    @Override
    public void beforeExecute(Context context) {
        ExecutionInput executionInput = context.unwrap(ExecutionInput.class);
        String operationName = getOperationName(executionInput);

        Scope scope = getTracer().buildSpan(operationName)
                .asChildOf(getTracer().activeSpan())
                .withTag("graphql.executionId", context.getExecutionId())
                .withTag("graphql.operationType", getOperationNameString(context.getRequestedOperationTypes()))
                .withTag("graphql.operationName", context.getOperationName().orElse(EMPTY))
                .startActive(true);

        scopes.put(context.getExecutionId(), scope);

        ((GraphQLContext) executionInput.getContext()).put(Span.class, scope.span()); // TODO: Do we need this ?
    }

    @Override
    public void afterExecute(Context context) {
        Scope scope = scopes.remove(context.getExecutionId());
        if (scope != null) {
            scope.close();
        }
    }

    @Override
    public void beforeDataFetch(Context context) {
        final DataFetchingEnvironment env = context.unwrap(DataFetchingEnvironment.class);

        Span parentSpan = getParentSpan(getTracer(), env);

        Scope scope = getTracer().buildSpan(getOperationName(env))
                .asChildOf(parentSpan)
                .withTag("graphql.executionId", context.getExecutionId())
                .withTag("graphql.operationType", getOperationNameString(context.getOperationType()))
                .withTag("graphql.operationName", context.getOperationName().orElse(EMPTY))
                .withTag("graphql.parent", context.getParentTypeName().orElse(EMPTY))
                .withTag("graphql.field", context.getFieldName())
                .withTag("graphql.path", context.getPath())
                .startActive(false);

        final Span span = scope.span();

        GraphQLContext graphQLContext = env.getContext();
        graphQLContext.put(Span.class, span);

        spans.put(context.getExecutionId(), span);
    }

    @Override
    public void errorDataFetch(String executionId, Throwable t) {

        Span span = spans.get(executionId);
        logError(span, t);

    }

    @Override
    public void errorExecute(String executionId, Throwable t) {
        Scope scope = scopes.remove(executionId);
        if (scope != null) {
            Map<String, Object> error = new HashMap<>();
            error.put("event.object", t);
            error.put("event", "error");
            scope.span().log(error);
            scope.close();
        }
    }

    @Override
    public void afterDataFetch(Context context) {
        Span span = spans.remove(context.getExecutionId());
        span.finish();

        /**
         * TODO
         *
         * @implNote The current scope must be closed in the thread in which it
         *           was opened, after work in the current thread has ended. So you can't
         *           use {@code before} because it is executed before the work in the
         *           current thread is finished, but you can't use {@code after} either
         *           because it can run in another thread.
         */
        getTracer().scopeManager().active().close();
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

    private String getOperationNameString(List<Context.OperationType> types) {
        List<String> typeStrings = new ArrayList<>();
        for (Context.OperationType type : types) {
            typeStrings.add(type.toString());
        }
        return String.join(UNDERSCORE, typeStrings);
    }

    private String getOperationNameString(Context.OperationType... types) {
        return getOperationNameString(Arrays.asList(types));
    }

    private static final String UNDERSCORE = "_";
    private static final String EMPTY = "";
    private static final String PREFIX = "GraphQL";
}
