package io.smallrye.graphql.execution.batchloader;

import java.util.List;
import java.util.concurrent.CompletionStage;

import org.dataloader.BatchLoaderEnvironment;
import org.dataloader.BatchLoaderWithContext;

import io.smallrye.graphql.bootstrap.Config;
import io.smallrye.graphql.schema.model.Operation;
import io.smallrye.graphql.spi.DataFetcherService;

/**
 * BatchLoader for List Source
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class SourceBatchLoader implements BatchLoaderWithContext<Object, Object> {

    private final DataFetcherService dataFetcherService;

    public SourceBatchLoader(Operation operation, Config config) {
        this.dataFetcherService = DataFetcherService.getDataFetcherService(operation);
        this.dataFetcherService.init(operation, config);
    }

    @Override
    @SuppressWarnings("unchecked")
    public CompletionStage<List<Object>> load(List<Object> keys, BatchLoaderEnvironment ble) {
        return dataFetcherService.batch(ble, keys);
    }
}
