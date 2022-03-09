package io.smallrye.graphql.execution.datafetcher;

import org.dataloader.DataLoader;
import org.eclipse.microprofile.context.ThreadContext;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import io.smallrye.graphql.execution.context.SmallRyeContext;
import io.smallrye.graphql.execution.datafetcher.helper.ArgumentHelper;
import io.smallrye.graphql.execution.datafetcher.helper.BatchLoaderHelper;
import io.smallrye.graphql.execution.datafetcher.helper.ContextHelper;
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
    private final BatchLoaderHelper batchLoaderHelper = new BatchLoaderHelper();
    private final ContextHelper contextHelper = new ContextHelper();
    private final EventEmitter eventEmitter = EventEmitter.getInstance();

    public BatchDataFetcher(Operation operation) {
        this.operation = operation;
        this.argumentHelper = new ArgumentHelper(operation.getArguments());
        this.batchLoaderName = batchLoaderHelper.getName(operation);
    }

    @Override
    public T get(final DataFetchingEnvironment dfe) throws Exception {

        SmallRyeContext smallryeContext = contextHelper.updateSmallRyeContextWithField(dfe, operation);
        eventEmitter.fireBeforeDataFetch(smallryeContext);
        Object[] transformedArguments = argumentHelper.getArguments(dfe, true);
        Object source = dfe.getSource();

        DataLoader<Object, Object> dataLoader = dfe.getDataLoader(batchLoaderName);
        batchLoaderHelper.setDataFetchingEnvironment(dataLoader, dfe);

        try {
            SmallRyeContext.setContext(smallryeContext);
            ThreadContext threadContext = ThreadContext.builder().build();
            return (T) threadContext.withContextCapture(dataLoader.load(source, transformedArguments));
        } finally {
            SmallRyeContext.remove();
        }
    }
}
