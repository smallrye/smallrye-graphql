package io.smallrye.graphql.execution.event;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.jboss.logging.Logger;

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
    private static final Logger LOG = Logger.getLogger(EventEmitter.class);
    private static final Map<Config, EventEmitter> INSTANCES = new IdentityHashMap<>();
    private final List<EventingService> enabledServices = new ArrayList<>();

    public static EventEmitter getInstance(Config config) {
        return INSTANCES.computeIfAbsent(config, k -> new EventEmitter(config));
    }

    private EventEmitter(Config config) {
        if (config != null) {
            ServiceLoader<EventingService> eventingServices = ServiceLoader.load(EventingService.class);
            Iterator<EventingService> it = eventingServices.iterator();

            EventingService eventingService = null;
            while (it.hasNext()) {
                try {
                    eventingService = it.next();
                    String configKey = eventingService.getConfigKey();
                    boolean enabled = config.getConfigValue(configKey, boolean.class, false);
                    if (enabled) {
                        enabledServices.add(eventingService);
                    }
                } catch (Throwable t) {
                    // Ignore that service...
                    Throwable cause = t.getCause();
                    LOG.warn("Failed to register " + t.getMessage()
                            + (cause != null ? "\n\tCaused by: " + cause.toString() : ""));
                }
            }
        }
    }

    // Execution 

    public void fireBeforeExecute() {
        Context context = SmallRyeContext.getContext();
        for (EventingService extensionService : enabledServices) {
            extensionService.beforeExecute(context);
        }
    }

    public void fireOnExecuteError(String executionId, Throwable t) {
        for (EventingService extensionService : enabledServices) {
            extensionService.errorExecute(executionId, t);
        }
    }

    public void fireAfterExecute() {
        Context context = SmallRyeContext.getContext();
        for (EventingService extensionService : enabledServices) {
            extensionService.afterExecute(context);
        }
    }

    // Execution - DataFetching
    public void fireBeforeDataFetch() {
        Context context = SmallRyeContext.getContext();
        for (EventingService extensionService : enabledServices) {
            extensionService.beforeDataFetch(context);
        }
    }

    public void fireBeforeMethodInvoke(InvokeInfo invokeInfo) throws Exception {
        for (EventingService extensionService : enabledServices) {
            extensionService.beforeInvoke(invokeInfo);
        }
    }

    public void fireOnDataFetchError(String executionId, Throwable t) {
        for (EventingService extensionService : enabledServices) {
            extensionService.errorDataFetch(executionId, t);
        }
    }

    public void fireAfterDataFetch() {
        Context context = SmallRyeContext.getContext();
        for (EventingService extensionService : enabledServices) {
            extensionService.afterDataFetch(context);
        }
    }

    // Schema bootstrap

    /**
     * This gets fired just before we create the final schema. This allows listeners to add to the builder any
     * custom elements.
     * 
     * @param builder as it stands
     * @return builder modified by listener
     */
    public GraphQLSchema.Builder fireBeforeSchemaBuild(GraphQLSchema.Builder builder) {

        for (EventingService extensionService : enabledServices) {
            builder = extensionService.beforeSchemaBuild(builder);
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
    public Operation fireCreateOperation(Operation operation) {
        for (EventingService extensionService : enabledServices) {
            operation = extensionService.createOperation(operation);
        }
        return operation;
    }
}
