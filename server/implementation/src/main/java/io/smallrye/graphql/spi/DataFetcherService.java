package io.smallrye.graphql.spi;

import io.smallrye.graphql.execution.datafetcher.PlugableBatchableDataFetcher;
import io.smallrye.graphql.execution.datafetcher.PlugableDataFetcher;
import io.smallrye.graphql.schema.model.Field;
import io.smallrye.graphql.schema.model.Operation;
import io.smallrye.graphql.schema.model.Reference;
import io.smallrye.graphql.schema.model.Type;

/**
 * DataFetcherService service that will get the datafetchers to use
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public interface DataFetcherService {

    public Integer getPriority();

    default PlugableBatchableDataFetcher getCompletionStageDataFetcher(Operation operation, Type type) {
        return null;
    }

    default PlugableBatchableDataFetcher getUniDataFetcher(Operation operation, Type type) {
        return null;
    }

    default PlugableBatchableDataFetcher getPublisherDataFetcher(Operation operation, Type type) {
        return null;
    }

    default PlugableBatchableDataFetcher getMultiDataFetcher(Operation operation, Type type) {
        return null;
    }

    default PlugableBatchableDataFetcher getOtherWrappedDataFetcher(Operation operation, Type type) {
        return null;
    }

    default PlugableBatchableDataFetcher getOtherFieldDataFetcher(Operation operation, Type type) {
        return null;
    }

    default PlugableBatchableDataFetcher getDefaultDataFetcher(Operation operation, Type type) {
        return null;
    }

    default PlugableDataFetcher getFieldDataFetcher(Field field, Type type, Reference owner) {
        return null;
    }
}
