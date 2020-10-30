package io.smallrye.graphql.execution.datafetcher;

import org.dataloader.DataLoader;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import io.smallrye.graphql.bootstrap.Config;
import io.smallrye.graphql.execution.context.SmallRyeContext;
import io.smallrye.graphql.execution.datafetcher.helper.ArgumentHelper;
import io.smallrye.graphql.execution.datafetcher.helper.BatchLoaderHelper;
import io.smallrye.graphql.execution.event.EventEmitter;
import io.smallrye.graphql.schema.model.Operation;

/**
 * Fetch data using using a batchloader
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class BatchDataFetcher<T> implements DataFetcher<T> {

    private final Operation operation;
    private final EventEmitter eventEmitter;
    private final ArgumentHelper argumentHelper;
    private final String batchLoaderName;
    private final BatchLoaderHelper batchLoaderHelper = new BatchLoaderHelper();

    public BatchDataFetcher(Operation operation, Config config) {
        this.operation = operation;
        this.eventEmitter = EventEmitter.getInstance(config);
        this.argumentHelper = new ArgumentHelper(operation.getArguments());
        this.batchLoaderName = batchLoaderHelper.getName(operation);
    }

    @Override
    public T get(final DataFetchingEnvironment dfe) throws Exception {
        SmallRyeContext.setDataFromFetcher(dfe, operation);
        eventEmitter.fireBeforeDataFetch();
        Object[] transformedArguments = argumentHelper.getArguments(dfe, true);
        Object source = dfe.getSource();
        DataLoader<Object, Object> dataLoader = dfe.getDataLoader(batchLoaderName);
        return (T) dataLoader.load(source, transformedArguments);
    }

}
