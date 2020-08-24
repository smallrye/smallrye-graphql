package io.smallrye.graphql.execution.batchloader;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.dataloader.BatchLoader;

import io.smallrye.graphql.execution.datafetcher.helper.ReflectionHelper;
import io.smallrye.graphql.schema.model.Operation;

/**
 * BatchLoader for List Source
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class SourceBatchLoader implements BatchLoader<Object, Object> {
    private final Operation operation;

    public SourceBatchLoader(Operation operation) {
        this.operation = operation;
    }

    @Override
    public CompletionStage<List<Object>> load(List<Object> keys) {
        return CompletableFuture.supplyAsync(() -> doSourceListCall(keys, this.operation));
    }

    private List<Object> doSourceListCall(List<Object> keys, Operation operation) {
        ReflectionHelper reflectionHelper = new ReflectionHelper(operation);
        try {
            return (List<Object>) reflectionHelper.invoke(keys);
        } catch (Exception ex) {
            // TODO: Handle this
            throw new RuntimeException(ex);
        }
    }
}
