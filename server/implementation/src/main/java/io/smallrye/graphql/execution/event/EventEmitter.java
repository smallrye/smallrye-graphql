package io.smallrye.graphql.execution.event;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

import graphql.schema.GraphQLSchema;
import io.smallrye.graphql.api.Context;
import io.smallrye.graphql.bootstrap.Config;
import io.smallrye.graphql.execution.context.SmallRyeContext;
import io.smallrye.graphql.schema.model.Operation;
import io.smallrye.graphql.spi.EventingService;

/**
 * Fire some events while booting or executing.
 * This allows some extension
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class EventEmitter {

    private static final ThreadLocal<EventEmitter> current = new ThreadLocal<>();

    public static void start(Config config) {
        if (config != null && config.shouldEmitEvents()) {
            EventEmitter eventEmitter = new EventEmitter(config);
            current.set(eventEmitter);
        }
    }

    public static void end() {
        current.remove();
    }

    // While building the schema

    /**
     * This gets fired just before we create the final schema. This allows listeners to add to the builder any
     * custom elements.
     * 
     * @param builder as it stands
     * @return builder modified by listener
     */
    public static GraphQLSchema.Builder fireBeforeSchemaBuild(GraphQLSchema.Builder builder) {
        Optional<EventEmitter> emitter = emitter();
        if (emitter.isPresent()) {
            return emitter.get().emitBeforeSchemaBuild(builder);
        }
        return builder;
    }

    /**
     * This gets fired during the bootstrap phase before a new operation
     * is being created. This allows listeners to modify the operation
     * 
     * @param operation as it stands
     * @return operation possibly modified
     */
    public static Operation fireCreateOperation(Operation operation) {
        Optional<EventEmitter> emitter = emitter();
        if (emitter.isPresent()) {
            return emitter.get().emitCreateOperation(operation);
        }
        return operation;
    }

    // While execuring requests

    public static void fireBeforeExecute() {
        emitter().ifPresent((t) -> t.emitBeforeExecute());
    }

    public static void fireOnExecuteError(String executionId, Throwable t) {
        Optional<EventEmitter> emitter = emitter();
        if (emitter.isPresent()) {
            emitter.get().emitOnExecuteError(executionId, t);
        }
    }

    public static void fireAfterExecute() {
        emitter().ifPresent((t) -> t.emitAfterExecute());
    }

    public static void fireBeforeDataFetch() {
        emitter().ifPresent((t) -> t.emitBeforeDataFetch());
    }

    public static void fireBeforeMethodInvoke(InvokeInfo invokeInfo) throws Exception {
        Optional<EventEmitter> emitter = emitter();
        if (emitter.isPresent()) {
            emitter.get().emitBeforeDataFetchMethodInvoke(invokeInfo);
        }
    }

    public static void fireOnDataFetchError(String executionId, Throwable t) {
        Optional<EventEmitter> emitter = emitter();
        if (emitter.isPresent()) {
            emitter.get().emitOnDataFetchError(executionId, t);
        }
    }

    public static void fireAfterDataFetch() {
        emitter().ifPresent((t) -> t.emitAfterDataFetch());
    }

    private static Optional<EventEmitter> emitter() {
        EventEmitter eventEmitter = current.get();
        if (eventEmitter != null) {
            return Optional.of(eventEmitter);
        } else {
            return Optional.empty();
        }
    }

    private final ServiceLoader<EventingService> eventingServices = ServiceLoader.load(EventingService.class);

    private final List<EventingService> enabledServices = new ArrayList<>();

    private EventEmitter(Config config) {
        Iterator<EventingService> it = eventingServices.iterator();
        while (it.hasNext()) {
            try {
                EventingService eventingService = it.next();
                String configKey = eventingService.getConfigKey();
                boolean enabled = config.getConfigValue(configKey, boolean.class, false);
                if (enabled) {
                    enabledServices.add(eventingService);
                }
            } catch (Throwable t) {
                // Ignore that service...
            }
        }
    }

    // Execution 

    private void emitBeforeExecute() {
        Context context = SmallRyeContext.getContext();
        for (EventingService extensionService : enabledServices) {
            extensionService.beforeExecute(context);
        }
    }

    private void emitOnExecuteError(String executionId, Throwable t) {
        for (EventingService extensionService : enabledServices) {
            extensionService.errorExecute(executionId, t);
        }
    }

    private void emitAfterExecute() {
        Context context = SmallRyeContext.getContext();
        for (EventingService extensionService : enabledServices) {
            extensionService.afterExecute(context);
        }
    }

    // Execution - DataFetching
    private void emitBeforeDataFetch() {
        Context context = SmallRyeContext.getContext();
        for (EventingService extensionService : enabledServices) {
            extensionService.beforeDataFetch(context);
        }
    }

    private void emitBeforeDataFetchMethodInvoke(InvokeInfo invokeInfo) throws Exception {
        for (EventingService extensionService : enabledServices) {
            extensionService.beforeInvoke(invokeInfo);
        }
    }

    private void emitOnDataFetchError(String executionId, Throwable t) {
        for (EventingService extensionService : enabledServices) {
            extensionService.errorDataFetch(executionId, t);
        }
    }

    private void emitAfterDataFetch() {
        Context context = SmallRyeContext.getContext();
        for (EventingService extensionService : enabledServices) {
            extensionService.afterDataFetch(context);
        }
    }

    // Schema bootstrap
    private GraphQLSchema.Builder emitBeforeSchemaBuild(GraphQLSchema.Builder builder) {
        for (EventingService extensionService : enabledServices) {
            builder = extensionService.beforeSchemaBuild(builder);
        }
        return builder;
    }

    private Operation emitCreateOperation(Operation operation) {
        for (EventingService extensionService : enabledServices) {
            operation = extensionService.createOperation(operation);
        }
        return operation;
    }
}
