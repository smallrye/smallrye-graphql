package io.smallrye.graphql.execution.datafetcher;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;

import org.dataloader.BatchLoaderEnvironment;
import org.eclipse.microprofile.graphql.GraphQLException;

import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetchingEnvironment;
import io.smallrye.graphql.api.Context;
import io.smallrye.graphql.execution.context.SmallRyeContext;
import io.smallrye.graphql.execution.context.SmallRyeContextManager;
import io.smallrye.graphql.execution.datafetcher.helper.ArgumentHelper;
import io.smallrye.graphql.execution.datafetcher.helper.BatchLoaderHelper;
import io.smallrye.graphql.execution.datafetcher.helper.ErrorResultHelper;
import io.smallrye.graphql.execution.datafetcher.helper.FieldHelper;
import io.smallrye.graphql.execution.datafetcher.helper.OperationInvoker;
import io.smallrye.graphql.execution.event.EventEmitter;
import io.smallrye.graphql.schema.model.Operation;
import io.smallrye.graphql.schema.model.Type;
import io.smallrye.graphql.transformation.AbstractDataFetcherException;

/**
 * The abstract data fetcher
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @param <K>
 * @param <T>
 */
public abstract class AbstractDataFetcher<K, T> implements PlugableBatchableDataFetcher<K, T> {

    protected Operation operation;
    protected Type type;
    protected FieldHelper fieldHelper;
    protected OperationInvoker operationInvoker;
    protected ErrorResultHelper errorResultHelper = new ErrorResultHelper();
    protected ArgumentHelper argumentHelper;
    protected EventEmitter eventEmitter = EventEmitter.getInstance();
    protected BatchLoaderHelper batchLoaderHelper = new BatchLoaderHelper();

    public AbstractDataFetcher(Operation operation, Type type) {
        this.operation = operation;
        this.type = type;
        this.fieldHelper = new FieldHelper(operation);
        this.operationInvoker = new OperationInvoker(operation);
        this.argumentHelper = new ArgumentHelper(operation.getArguments());
    }

    @Override
    public T get(final DataFetchingEnvironment dfe) throws Exception {
        // update the context
        SmallRyeContext smallRyeContext = SmallRyeContextManager.populateFromDataFetchingEnvironment(type, operation, dfe);
        eventEmitter.fireBeforeDataFetch(smallRyeContext);

        final DataFetcherResult.Builder<Object> resultBuilder = DataFetcherResult.newResult()
                .localContext(dfe.getGraphQlContext());

        try {
            List<Object> transformedArguments = argumentHelper.getArguments(dfe);

            return invokeAndTransform(smallRyeContext, dfe, resultBuilder, transformedArguments.toArray());
        } catch (AbstractDataFetcherException abstractDataFetcherException) {
            //Arguments or result couldn't be transformed
            abstractDataFetcherException.appendDataFetcherResult(resultBuilder, dfe);
            eventEmitter.fireOnDataFetchError(smallRyeContext, abstractDataFetcherException);
        } catch (GraphQLException graphQLException) {
            errorResultHelper.appendPartialResult(resultBuilder, dfe, graphQLException);
            eventEmitter.fireOnDataFetchError(smallRyeContext, graphQLException);
        } catch (Throwable ex) {
            eventEmitter.fireOnDataFetchError(smallRyeContext, ex);
            throw ex;
        }

        return invokeFailure(resultBuilder);
    }

    @Override
    public CompletionStage<List<T>> load(List<K> keys, BatchLoaderEnvironment ble) {
        Map<String, Object> batchContext = batchLoaderHelper.getBatchContext(keys, ble);
        Object[] arguments = batchLoaderHelper.getArguments(batchContext);
        DataFetchingEnvironment dataFetchingEnvironment = batchLoaderHelper.getDataFetchingEnvironment(batchContext);

        try {
            SmallRyeContext smallRyeContext = SmallRyeContextManager.populateFromDataFetchingEnvironment(type, operation,
                    dataFetchingEnvironment);
            return invokeBatch(dataFetchingEnvironment, arguments);
        } finally {
            SmallRyeContextManager.clearCurrentSmallRyeContext();
        }
    }

    protected abstract <T> T invokeAndTransform(Context c, DataFetchingEnvironment dfe,
            DataFetcherResult.Builder<Object> resultBuilder,
            Object[] transformedArguments) throws AbstractDataFetcherException, Exception;

    protected abstract <T> T invokeFailure(DataFetcherResult.Builder<Object> resultBuilder);

    protected abstract CompletionStage<List<T>> invokeBatch(DataFetchingEnvironment dfe, Object[] arguments);
}
