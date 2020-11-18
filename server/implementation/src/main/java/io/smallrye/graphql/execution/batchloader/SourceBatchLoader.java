package io.smallrye.graphql.execution.batchloader;

import java.util.List;
import java.util.concurrent.CompletionStage;

import org.dataloader.BatchLoaderEnvironment;
import org.dataloader.BatchLoaderWithContext;

import io.smallrye.graphql.bootstrap.Config;
import io.smallrye.graphql.schema.model.Operation;
import io.smallrye.graphql.spi.WrapperHandlerService;

/**
 * BatchLoader for List Source
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class SourceBatchLoader implements BatchLoaderWithContext<Object, Object> {

    private final WrapperHandlerService wrapperHandlerService;
    private final Operation operation;

    public SourceBatchLoader(Operation operation, Config config) {
        this.operation = operation;
        this.wrapperHandlerService = WrapperHandlerService.getWrapperHandlerService(operation);
        this.wrapperHandlerService.initDataFetcher(operation, config);
    }

    @Override
    @SuppressWarnings("unchecked")
    public CompletionStage<List<Object>> load(List<Object> keys, BatchLoaderEnvironment ble) {
        return wrapperHandlerService.getBatchData(ble, keys);
    }
}
