package io.smallrye.graphql.execution.batchloader;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.dataloader.BatchLoaderEnvironment;
import org.dataloader.BatchLoaderWithContext;

import io.smallrye.graphql.api.Context;
import io.smallrye.graphql.bootstrap.Config;
import io.smallrye.graphql.execution.context.SmallRyeContext;
import io.smallrye.graphql.execution.datafetcher.helper.ReflectionHelper;
import io.smallrye.graphql.execution.event.EventEmitter;
import io.smallrye.graphql.schema.model.Operation;

/**
 * BatchLoader for List Source
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class SourceBatchLoader implements BatchLoaderWithContext<Object, Object> {

    private final ReflectionHelper reflectionHelper;
    private final boolean async;

    public SourceBatchLoader(Operation operation, Config config) {
        EventEmitter eventEmitter = EventEmitter.getInstance(config);
        this.reflectionHelper = new ReflectionHelper(operation, eventEmitter);
        async = operation.isAsync();
    }

    @Override
    @SuppressWarnings("unchecked")
    public CompletionStage<List<Object>> load(List<Object> keys, BatchLoaderEnvironment ble) {
        Context context = SmallRyeContext.getContext();
        Object[] arguments = getArguments(keys, ble);

        final ClassLoader tccl = Thread.currentThread().getContextClassLoader();

        if (async) {
            return (CompletableFuture<List<Object>>) invokePrivileged(context, arguments, tccl);
        } else {
            return CompletableFuture.supplyAsync(() -> (List<Object>) invokePrivileged(context, arguments, tccl));
        }
    }

    private Object invokePrivileged(Context context, Object[] arguments, ClassLoader tccl) {
        try {
            return AccessController
                    .doPrivileged(new PrivilegedExceptionAction<Object>() {
                        @Override
                        public Object run() {
                            ClassLoader originalTccl = Thread.currentThread()
                                    .getContextClassLoader();
                            Thread.currentThread().setContextClassLoader(tccl);
                            try {
                                return doSourceCall(arguments, context);
                            } finally {
                                if (originalTccl != null) {
                                    Thread.currentThread().setContextClassLoader(tccl);
                                }
                            }
                        }
                    });
        } catch (PrivilegedActionException e) {
            throw new RuntimeException(e);
        }
    }

    private Object doSourceCall(Object[] arguments, Context context) {
        SmallRyeContext.register(context.getRequest()); // For this thread

        try {
            return reflectionHelper.invoke(arguments);
        } catch (Exception ex) {
            // TODO: Handle this
            throw new RuntimeException(ex);
        }
    }

    private Object[] getArguments(List<Object> keys, BatchLoaderEnvironment ble) {

        List<Object> arguments = new ArrayList<>();
        arguments.add(keys);

        List<Object> keyContextsList = ble.getKeyContextsList();
        if (keyContextsList != null && !keyContextsList.isEmpty()) {
            Object[] otherArguments = (Object[]) keyContextsList.get(0);
            arguments.addAll(Arrays.asList(otherArguments));
        }

        return arguments.toArray();
    }

}
