package io.smallrye.graphql.execution.datafetcher;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dataloader.DataLoader;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import io.smallrye.graphql.execution.context.SmallRyeContext;
import io.smallrye.graphql.execution.context.SmallRyeContextManager;
import io.smallrye.graphql.execution.datafetcher.helper.ArgumentHelper;
import io.smallrye.graphql.execution.datafetcher.helper.BatchLoaderHelper;
import io.smallrye.graphql.execution.event.EventEmitter;
import io.smallrye.graphql.execution.metrics.MetricsEmitter;
import io.smallrye.graphql.schema.model.Operation;
import io.smallrye.graphql.schema.model.Type;
import io.smallrye.mutiny.Uni;

/**
 * Fetch data using using a batchloader
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class BatchDataFetcher<T> implements DataFetcher<T> {

    private final Operation operation;
    private final Type type;
    private final ArgumentHelper argumentHelper;
    private final String batchLoaderName;
    private final BatchLoaderHelper batchLoaderHelper = new BatchLoaderHelper();
    private final EventEmitter eventEmitter = EventEmitter.getInstance();
    private final MetricsEmitter metricsEmitter = MetricsEmitter.getInstance();

    public BatchDataFetcher(Operation operation, Type type) {
        this.operation = operation;
        this.type = type;
        this.argumentHelper = new ArgumentHelper(operation.getArguments());
        this.batchLoaderName = batchLoaderHelper.getName(operation);
    }

    @Override
    public T get(final DataFetchingEnvironment dfe) throws Exception {

        SmallRyeContext smallryeContext = SmallRyeContextManager.populateFromDataFetchingEnvironment(type, operation, dfe);
        eventEmitter.fireBeforeDataFetch(smallryeContext);
        long measurementId = metricsEmitter.start(smallryeContext);

        try {
            List<Object> transformedArguments = argumentHelper.getArguments(dfe, true);
            Object source = dfe.getSource();

            DataLoader<Object, Object> dataLoader = dfe.getDataLoader(batchLoaderName);

            Map<String, Object> batchContext = new HashMap<>();
            batchContext.put(BatchLoaderHelper.ARGUMENTS, transformedArguments);
            batchContext.put(BatchLoaderHelper.DATA_FETCHING_ENVIRONMENT, dfe);

            return (T) Uni.createFrom().completionStage(() -> dataLoader.load(source, batchContext)).onItemOrFailure()
                    .invoke(() -> metricsEmitter.end(measurementId))
                    .subscribe()
                    .asCompletionStage();
        } catch (Exception e) {
            metricsEmitter.end(measurementId);
            throw e;
        }
    }
}
