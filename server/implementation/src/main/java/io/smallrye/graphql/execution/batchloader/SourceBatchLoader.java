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
import io.smallrye.graphql.execution.context.SmallRyeContext;
import io.smallrye.graphql.execution.datafetcher.helper.ReflectionHelper;
import io.smallrye.graphql.schema.model.Operation;

/**
 * BatchLoader for List Source
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class SourceBatchLoader implements BatchLoaderWithContext<Object, Object> {
    private final Operation operation;
    private final ReflectionHelper reflectionHelper;

    public SourceBatchLoader(Operation operation) {
        this.operation = operation;
        this.reflectionHelper = new ReflectionHelper(operation);
    }

    @Override
    public CompletionStage<List<Object>> load(List<Object> keys, BatchLoaderEnvironment ble) {
        Context context = SmallRyeContext.getContext();
        Object[] arguments = getArguments(keys, ble);

        final ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        return CompletableFuture.supplyAsync(() -> {
            try {
                return AccessController.doPrivileged(new PrivilegedExceptionAction<List<Object>>() {
                    @Override
                    public List<Object> run() {
                        ClassLoader originalTccl = Thread.currentThread().getContextClassLoader();
                        Thread.currentThread().setContextClassLoader(tccl);
                        try {
                            return doSourceListCall(arguments, context);
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
        });
    }

    private List<Object> doSourceListCall(Object[] arguments, Context context) {
        SmallRyeContext.register(context.getRequest()); // For this thread

        try {
            return (List<Object>) reflectionHelper.invoke(arguments);
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
