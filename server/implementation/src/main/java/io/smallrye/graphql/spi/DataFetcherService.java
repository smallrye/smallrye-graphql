package io.smallrye.graphql.spi;

import io.smallrye.graphql.execution.datafetcher.PlugableDataFetcher;
import io.smallrye.graphql.schema.model.Operation;
import io.smallrye.graphql.schema.model.Type;

/**
 * DataFetcherService service that will get the datafetchers to use
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public interface DataFetcherService {

    public Integer getPriority();

    default PlugableDataFetcher getCompletionStageDataFetcher(Operation operation, Type type) {
        return null;
    }

    default PlugableDataFetcher getUniDataFetcher(Operation operation, Type type) {
        return null;
    }

    default PlugableDataFetcher getPublisherDataFetcher(Operation operation, Type type) {
        return null;
    }

    default PlugableDataFetcher getMultiDataFetcher(Operation operation, Type type) {
        return null;
    }

    default PlugableDataFetcher getOtherWrappedDataFetcher(Operation operation, Type type) {
        return null;
    }

    default PlugableDataFetcher getOtherFieldDataFetcher(Operation operation, Type type) {
        return null;
    }

    default PlugableDataFetcher getDefaultDataFetcher(Operation operation, Type type) {
        return null;
    }
}
