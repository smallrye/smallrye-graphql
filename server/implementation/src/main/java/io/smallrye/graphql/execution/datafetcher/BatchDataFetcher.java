package io.smallrye.graphql.execution.datafetcher;

import org.dataloader.DataLoader;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import io.smallrye.graphql.execution.context.SmallRyeContextManager;
import io.smallrye.graphql.execution.datafetcher.helper.ArgumentHelper;
import io.smallrye.graphql.execution.datafetcher.helper.BatchLoaderHelper;
import io.smallrye.graphql.execution.event.EventEmitter;
import io.smallrye.graphql.schema.model.Operation;
import io.smallrye.mutiny.Uni;

/**
 * Fetch data using using a batchloader
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class BatchDataFetcher<T> implements DataFetcher<T> {

    private final Operation operation;
    private final ArgumentHelper argumentHelper;
    private final String batchLoaderName;
    private final BatchLoaderHelper batchLoaderHelper = new BatchLoaderHelper();
    private final EventEmitter eventEmitter = EventEmitter.getInstance();

    public BatchDataFetcher(Operation operation) {
        this.operation = operation;
        this.argumentHelper = new ArgumentHelper(operation.getArguments());
        this.batchLoaderName = batchLoaderHelper.getName(operation);
    }

    @Override
    public T get(final DataFetchingEnvironment dfe) throws Exception {
        SmallRyeContextManager.populateFromDataFetchingEnvironment(operation, dfe);
        eventEmitter.fireBeforeDataFetch(SmallRyeContextManager.getCurrentSmallRyeContext());
        Object[] transformedArguments = argumentHelper.getArguments(dfe, true);
        Object source = dfe.getSource();

        DataLoader<Object, Object> dataLoader = dfe.getDataLoader(batchLoaderName);

        return (T) Uni.createFrom().completionStage(() -> dataLoader.load(source, transformedArguments)).subscribe()
                .asCompletionStage();
    }
}
