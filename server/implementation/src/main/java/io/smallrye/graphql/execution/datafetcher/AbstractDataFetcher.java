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
import io.smallrye.graphql.execution.datafetcher.helper.FieldHelper;
import io.smallrye.graphql.execution.datafetcher.helper.PartialResultHelper;
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
    protected PartialResultHelper partialResultHelper;
    protected ArgumentHelper argumentHelper;
    protected EventEmitter eventEmitter;
    protected BatchLoaderHelper batchLoaderHelper;

    public AbstractDataFetcher(Operation operation, Config config) {
        this.operation = operation;
        this.eventEmitter = EventEmitter.getInstance(config);
        this.fieldHelper = new FieldHelper(operation);
        this.reflectionHelper = new ReflectionHelper(operation, eventEmitter);
        this.argumentHelper = new ArgumentHelper(operation.getArguments());
        this.partialResultHelper = new PartialResultHelper();
        this.batchLoaderHelper = new BatchLoaderHelper();
    }

    @Override
    public T get(final DataFetchingEnvironment dfe) throws Exception {
        // update the context
        GraphQLContext graphQLContext = dfe.getContext();
        SmallRyeContext context = ((SmallRyeContext) graphQLContext.get("context")).withDataFromFetcher(dfe, operation);
        graphQLContext.put("context", context);

        final DataFetcherResult.Builder<Object> resultBuilder = DataFetcherResult.newResult().localContext(graphQLContext);

        eventEmitter.fireBeforeDataFetch(context);

        try {
            Object[] transformedArguments = argumentHelper.getArguments(dfe);

            return invokeAndTransform(dfe, resultBuilder, transformedArguments);
        } catch (AbstractDataFetcherException abstractDataFetcherException) {
            //Arguments or result couldn't be transformed
            abstractDataFetcherException.appendDataFetcherResult(resultBuilder, dfe);
            eventEmitter.fireOnDataFetchError(dfe.getExecutionId().toString(), abstractDataFetcherException);
        } catch (GraphQLException graphQLException) {
            partialResultHelper.appendPartialResult(resultBuilder, dfe, graphQLException);
            eventEmitter.fireOnDataFetchError(dfe.getExecutionId().toString(), graphQLException);
        } catch (SecurityException | IllegalAccessException | IllegalArgumentException ex) {
            //m.invoke failed
            eventEmitter.fireOnDataFetchError(dfe.getExecutionId().toString(), ex);
            throw ex;
        } finally {
            eventEmitter.fireAfterDataFetch(context);
        }

        return invokeFailure(resultBuilder);
    }

    protected abstract <T> T invokeAndTransform(DataFetchingEnvironment dfe, DataFetcherResult.Builder<Object> resultBuilder,
            Object[] transformedArguments) throws AbstractDataFetcherException, Exception;

    protected abstract <T> T invokeFailure(DataFetcherResult.Builder<Object> resultBuilder);

}
