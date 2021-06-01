package io.smallrye.graphql.execution.datafetcher;

import org.dataloader.BatchLoaderWithContext;
import org.eclipse.microprofile.graphql.GraphQLException;

import graphql.GraphQLContext;
import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import io.smallrye.graphql.bootstrap.Config;
import io.smallrye.graphql.execution.context.SmallRyeContext;
import io.smallrye.graphql.execution.datafetcher.helper.ArgumentHelper;
import io.smallrye.graphql.execution.datafetcher.helper.BatchLoaderHelper;
import io.smallrye.graphql.execution.datafetcher.helper.ErrorResultHelper;
import io.smallrye.graphql.execution.datafetcher.helper.FieldHelper;
import io.smallrye.graphql.execution.datafetcher.helper.ReflectionHelper;
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
public abstract class AbstractDataFetcher<K, T> implements DataFetcher<T>, BatchLoaderWithContext<K, T> {

    protected Operation operation;
    protected FieldHelper fieldHelper;
    protected ReflectionHelper reflectionHelper;
    protected ErrorResultHelper errorResultHelper;
    protected ArgumentHelper argumentHelper;
    protected EventEmitter eventEmitter;
    protected BatchLoaderHelper batchLoaderHelper;

    public AbstractDataFetcher(Operation operation, Config config) {
        this.operation = operation;
        this.eventEmitter = EventEmitter.getInstance(config);
        this.fieldHelper = new FieldHelper(operation);
        this.reflectionHelper = new ReflectionHelper(operation, eventEmitter);
        this.argumentHelper = new ArgumentHelper(operation.getArguments());
        this.errorResultHelper = new ErrorResultHelper(config);
        this.batchLoaderHelper = new BatchLoaderHelper();
    }

    @Override
    public T get(final DataFetchingEnvironment dfe) throws Exception {
        // update the context
        SmallRyeContext context = getSmallRyeContext(dfe);
        final DataFetcherResult.Builder<Object> resultBuilder = DataFetcherResult.newResult().localContext(dfe.getContext());

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

    protected SmallRyeContext getSmallRyeContext(final DataFetchingEnvironment dfe) {
        // update the context
        GraphQLContext graphQLContext = dfe.getContext();
        SmallRyeContext context = graphQLContext.get("context");
        if (context != null) {
            context = context.withDataFromFetcher(dfe, operation);
            graphQLContext.put("context", context);
        }

        eventEmitter.fireBeforeDataFetch(context);

        return context;
    }

    protected abstract <T> T invokeAndTransform(DataFetchingEnvironment dfe, DataFetcherResult.Builder<Object> resultBuilder,
            Object[] transformedArguments) throws AbstractDataFetcherException, Exception;

    protected abstract <T> T invokeFailure(DataFetcherResult.Builder<Object> resultBuilder);

}
