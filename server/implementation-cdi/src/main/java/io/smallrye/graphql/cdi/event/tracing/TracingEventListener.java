package io.smallrye.graphql.cdi.event.tracing;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionException;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

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
import io.smallrye.graphql.cdi.config.GraphQLConfig;
import io.smallrye.graphql.cdi.event.annotation.AfterDataFetch;
import io.smallrye.graphql.cdi.event.annotation.AfterExecute;
import io.smallrye.graphql.cdi.event.annotation.BeforeDataFetch;
import io.smallrye.graphql.cdi.event.annotation.BeforeExecute;
import io.smallrye.graphql.cdi.event.annotation.ErrorDataFetch;
import io.smallrye.graphql.cdi.event.annotation.ErrorExecute;

/**
 * Listening for event and create traces from it
 * 
 * @author Jan Martiska (jmartisk@redhat.com)
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@ApplicationScoped
public class TracingEventListener {

    private static final Object PARENT_SPAN_KEY = Span.class;

    private final Map<Context, Span> spans = Collections.synchronizedMap(new IdentityHashMap<>());
    private final Map<Context, Scope> scopes = Collections.synchronizedMap(new IdentityHashMap<>());

    @Inject
    Tracer tracer;

    @Inject
    GraphQLConfig graphQLConfig;

    public void beforeExecute(@Observes @BeforeExecute Context context) {
        if (graphQLConfig.isTracingEnabled()) {
            ExecutionInput executionInput = context.unwrap(ExecutionInput.class);
            String operationName = getOperationName(executionInput);

            Scope scope = tracer.buildSpan(operationName)
                    .asChildOf(tracer.activeSpan())
                    .withTag("graphql.executionId", context.getExecutionId())
                    .withTag("graphql.operationType", getOperationNameString(context.getRequestedOperationTypes()))
                    .withTag("graphql.operationName", context.getOperationName().orElse(EMPTY))
                    .startActive(true);

            scopes.put(context, scope);

            ((GraphQLContext) executionInput.getContext()).put(Span.class, scope.span()); // TODO: Do we need this ?
        }
    }

    public void errorExecute(@Observes @ErrorExecute Context context) {
        Scope scope = scopes.remove(context);
        if (scope != null) {
            Map<String, Object> error = new HashMap<>();
            Throwable throwable = context.getExceptionStack().peek();
            if (throwable != null) {
                error.put("event.object", throwable);
            }
            error.put("event", "error");
            scope.span().log(error);
            scope.close();
        }
    }

    public void afterExecute(@Observes @AfterExecute Context context) {
        Scope scope = scopes.remove(context);
        if (scope != null) {
            scope.close();
        }
    }

    public void beforeDataLoad(@Observes @BeforeDataFetch Context context) {
        if (graphQLConfig.isTracingEnabled()) {
            final DataFetchingEnvironment env = context.unwrap(DataFetchingEnvironment.class);

            Span parentSpan = getParentSpan(tracer, env);

            Scope scope = tracer.buildSpan(getOperationName(env))
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
            graphQLContext.put(PARENT_SPAN_KEY, span);

            spans.put(context, span);
        }
    }

    public void errorDataLoad(@Observes @ErrorDataFetch Context context) {
        if (graphQLConfig.isTracingEnabled() && context.hasException()) {
            Throwable t = context.getExceptionStack().peek();
            Span span = spans.get(context);
            logError(span, t);
        }
    }

    public void afterDataLoad(@Observes @AfterDataFetch Context context) {
        if (graphQLConfig.isTracingEnabled()) {
            Span span = spans.remove(context);
            span.finish();

            /**
             * TODO
             * 
             * @implNote The current scope must be closed in the thread in which it was opened,
             *           after work in the current thread has ended.
             *           So you can't use {@code before} because it is executed before the work in the current thread is
             *           finished,
             *           but you can't use {@code after} either because it can run in another thread.
             */
            tracer.scopeManager().active().close();
        }
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
