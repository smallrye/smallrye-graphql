package io.smallrye.graphql.execution.datafetcher;

import org.dataloader.DataLoader;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import io.smallrye.graphql.schema.model.Operation;

/**
 * Fetch data using using a batchloader
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class BatchDataFetcher<T> implements DataFetcher<T> {

    private final Operation operation;

    public BatchDataFetcher(Operation operation) {
        this.operation = operation;
    }

    @Override
    public T get(final DataFetchingEnvironment dfe) throws Exception {
        Object source = dfe.getSource();
        DataLoader<Object, Object> dataLoader = dfe.getDataLoader(operation.getName());
        return (T) dataLoader.load(source);
    }

}
