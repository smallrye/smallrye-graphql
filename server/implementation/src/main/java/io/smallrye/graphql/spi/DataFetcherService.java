package io.smallrye.graphql.spi;

import io.smallrye.graphql.execution.datafetcher.PlugableDataFetcher;
import io.smallrye.graphql.schema.model.Operation;

/**
 * DataFetcherService service that will get the datafetchers to use
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public interface DataFetcherService {

    public Integer getPriority();

    default PlugableDataFetcher getCompletionStageDataFetcher(Operation operation) {
        return null;
    }

    default PlugableDataFetcher getUniDataFetcher(Operation operation) {
        return null;
    }

    default PlugableDataFetcher getPublisherDataFetcher(Operation operation) {
        return null;
    }

    default PlugableDataFetcher getMultiDataFetcher(Operation operation) {
        return null;
    }

    default PlugableDataFetcher getOtherWrappedDataFetcher(Operation operation) {
        return null;
    }

    default PlugableDataFetcher getOtherFieldDataFetcher(Operation operation) {
        return null;
    }

    default PlugableDataFetcher getDefaultDataFetcher(Operation operation) {
        return null;
    }
}
