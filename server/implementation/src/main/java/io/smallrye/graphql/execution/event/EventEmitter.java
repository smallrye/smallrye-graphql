package io.smallrye.graphql.execution.event;

import java.util.Iterator;
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
        if (config != null && config.isEventingEnabled()) {
            EventEmitter eventEmitter = new EventEmitter();
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
            return emitter.get().emmitBeforeSchemaBuild(builder);
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
            return emitter.get().emmitCreateOperation(operation);
        }
        return operation;
    }

    // While execuring requests

    public static void fireBeforeExecute() {
        emitter().ifPresent((t) -> t.emmitBeforeExecute());
    }

    public static void fireOnExecuteError() {
        emitter().ifPresent((t) -> t.emmitOnExecuteError());
    }

    public static void fireAfterExecute() {
        emitter().ifPresent((t) -> t.emmitAfterExecute());
    }

    public static void fireBeforeDataFetch() {
        emitter().ifPresent((t) -> t.emmitBeforeDataFetch());
    }

    public static void fireOnDataFetchError() {
        emitter().ifPresent((t) -> t.emmitOnDataFetchError());
    }

    public static void fireAfterDataFetch() {
        emitter().ifPresent((t) -> t.emmitAfterDataFetch());
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

    private EventEmitter() {
    }

    // Execution 

    private void emmitBeforeExecute() {
        Context context = SmallRyeContext.getContext();
        Iterator<EventingService> it = eventingServices.iterator();

        while (it.hasNext()) {
            EventingService extensionService = it.next();
            extensionService.beforeExecute(context);
        }
    }

    private void emmitOnExecuteError() {
        Context context = SmallRyeContext.getContext();
        Iterator<EventingService> it = eventingServices.iterator();

        while (it.hasNext()) {
            EventingService extensionService = it.next();
            extensionService.errorExecute(context);
        }
    }

    private void emmitAfterExecute() {
        Context context = SmallRyeContext.getContext();
        Iterator<EventingService> it = eventingServices.iterator();

        while (it.hasNext()) {
            EventingService extensionService = it.next();
            extensionService.afterExecute(context);
        }
    }

    // Execution - DataFetching
    private void emmitBeforeDataFetch() {
        Context context = SmallRyeContext.getContext();
        Iterator<EventingService> it = eventingServices.iterator();

        while (it.hasNext()) {
            EventingService extensionService = it.next();
            extensionService.beforeDataFetch(context);
        }
    }

    private void emmitOnDataFetchError() {
        Context context = SmallRyeContext.getContext();
        Iterator<EventingService> it = eventingServices.iterator();

        while (it.hasNext()) {
            EventingService extensionService = it.next();
            extensionService.errorDataFetch(context);
        }
    }

    private void emmitAfterDataFetch() {
        Context context = SmallRyeContext.getContext();
        Iterator<EventingService> it = eventingServices.iterator();

        while (it.hasNext()) {
            EventingService extensionService = it.next();
            extensionService.afterDataFetch(context);
        }
    }

    // Schema bootstrap
    private GraphQLSchema.Builder emmitBeforeSchemaBuild(GraphQLSchema.Builder builder) {
        Iterator<EventingService> it = eventingServices.iterator();

        while (it.hasNext()) {
            EventingService extensionService = it.next();
            builder = extensionService.beforeSchemaBuild(builder);
        }

        return builder;
    }

    private Operation emmitCreateOperation(Operation operation) {
        Iterator<EventingService> it = eventingServices.iterator();

        while (it.hasNext()) {
            EventingService extensionService = it.next();
            operation = extensionService.createOperation(operation);
        }

        return operation;
    }
}
