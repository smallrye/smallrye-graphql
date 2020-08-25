package io.smallrye.graphql.execution.datafetcher;

import org.dataloader.DataLoader;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import io.smallrye.graphql.execution.batchloader.SourceBatchLoaderHelper;
import io.smallrye.graphql.execution.context.SmallRyeContext;
import io.smallrye.graphql.execution.datafetcher.helper.ArgumentHelper;
import io.smallrye.graphql.execution.event.EventEmitter;
import io.smallrye.graphql.schema.model.Operation;

/**
 * Fetch data using using a batchloader
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class BatchDataFetcher<T> implements DataFetcher<T> {

    private final Operation operation;
    private final ArgumentHelper argumentHelper;
    private final String batchLoaderName;

    public BatchDataFetcher(Operation operation) {
        this.operation = operation;
        this.argumentHelper = new ArgumentHelper(operation.getArguments());
        this.batchLoaderName = SourceBatchLoaderHelper.getName(operation);
    }

    @Override
    public T get(final DataFetchingEnvironment dfe) throws Exception {
        SmallRyeContext.setDataFromFetcher(dfe, operation);
        EventEmitter.fireBeforeDataFetch();
        Object[] transformedArguments = argumentHelper.getArguments(dfe, true);
        Object source = dfe.getSource();
        DataLoader<Object, Object> dataLoader = dfe.getDataLoader(batchLoaderName);
        return (T) dataLoader.load(source, transformedArguments);
    }

}
