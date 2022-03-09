package io.smallrye.graphql.execution.datafetcher;

import org.eclipse.microprofile.graphql.GraphQLException;

import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetchingEnvironment;
import io.smallrye.graphql.execution.context.SmallRyeContext;
import io.smallrye.graphql.execution.datafetcher.helper.ArgumentHelper;
import io.smallrye.graphql.execution.datafetcher.helper.BatchLoaderHelper;
import io.smallrye.graphql.execution.datafetcher.helper.ErrorResultHelper;
import io.smallrye.graphql.execution.datafetcher.helper.FieldHelper;
import io.smallrye.graphql.execution.datafetcher.helper.OperationInvoker;
import io.smallrye.graphql.execution.event.EventEmitter;
import io.smallrye.graphql.schema.model.Operation;
import io.smallrye.graphql.transformation.AbstractDataFetcherException;

/**
 * The abstract data fetcher
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @param <K>
 * @param <T>
 */
public abstract class AbstractDataFetcher<K, T> implements PlugableDataFetcher<K, T>, ContextAware {

    protected Operation operation;
    protected FieldHelper fieldHelper;
    protected OperationInvoker operationInvoker;
    protected ErrorResultHelper errorResultHelper = new ErrorResultHelper();
    protected ArgumentHelper argumentHelper;
    protected EventEmitter eventEmitter = EventEmitter.getInstance();
    protected BatchLoaderHelper batchLoaderHelper = new BatchLoaderHelper();

    public AbstractDataFetcher(Operation operation) {
        this.operation = operation;
        this.fieldHelper = new FieldHelper(operation);
        this.operationInvoker = new OperationInvoker(operation);
        this.argumentHelper = new ArgumentHelper(operation.getArguments());
    }

    @Override
    public T get(final DataFetchingEnvironment dfe) throws Exception {
        // update the context
        SmallRyeContext context = initSmallRyeContext(dfe);
        final DataFetcherResult.Builder<Object> resultBuilder = DataFetcherResult.newResult()
                .localContext(dfe.getGraphQlContext());

        try {
            Object[] transformedArguments = argumentHelper.getArguments(dfe);

            return invokeAndTransform(dfe, resultBuilder, transformedArguments);
        } catch (AbstractDataFetcherException abstractDataFetcherException) {
            //Arguments or result couldn't be transformed
            abstractDataFetcherException.appendDataFetcherResult(resultBuilder, dfe);
            eventEmitter.fireOnDataFetchError(dfe.getExecutionId().toString(), abstractDataFetcherException);
        } catch (GraphQLException graphQLException) {
            errorResultHelper.appendPartialResult(resultBuilder, dfe, graphQLException);
            eventEmitter.fireOnDataFetchError(dfe.getExecutionId().toString(), graphQLException);
        } catch (Throwable ex) {
            eventEmitter.fireOnDataFetchError(dfe.getExecutionId().toString(), ex);
            throw ex;
        } finally {
            eventEmitter.fireAfterDataFetch(context);
        }

        return invokeFailure(resultBuilder);
    }

    private SmallRyeContext initSmallRyeContext(final DataFetchingEnvironment dfe) {
        // update the context
        SmallRyeContext context = updateSmallRyeContext(dfe, operation);
        eventEmitter.fireBeforeDataFetch(context);
        return context;
    }

    protected abstract <T> T invokeAndTransform(DataFetchingEnvironment dfe, DataFetcherResult.Builder<Object> resultBuilder,
            Object[] transformedArguments) throws AbstractDataFetcherException, Exception;

    protected abstract <T> T invokeFailure(DataFetcherResult.Builder<Object> resultBuilder);

}
