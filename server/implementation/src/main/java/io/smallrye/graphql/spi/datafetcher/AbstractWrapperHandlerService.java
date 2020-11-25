package io.smallrye.graphql.spi.datafetcher;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionException;

import org.eclipse.microprofile.graphql.GraphQLException;

import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetchingEnvironment;
import io.smallrye.graphql.bootstrap.Config;
import io.smallrye.graphql.execution.datafetcher.helper.ArgumentHelper;
import io.smallrye.graphql.execution.datafetcher.helper.BatchLoaderHelper;
import io.smallrye.graphql.execution.datafetcher.helper.FieldHelper;
import io.smallrye.graphql.execution.datafetcher.helper.PartialResultHelper;
import io.smallrye.graphql.execution.datafetcher.helper.ReflectionHelper;
import io.smallrye.graphql.execution.event.EventEmitter;
import io.smallrye.graphql.schema.model.Operation;
import io.smallrye.graphql.spi.WrapperHandlerService;
import io.smallrye.graphql.transformation.AbstractDataFetcherException;

/**
 * An Abstract handler that implements some of the common methods
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public abstract class AbstractWrapperHandlerService implements WrapperHandlerService {
    protected Operation operation;
    protected FieldHelper fieldHelper;
    protected ReflectionHelper reflectionHelper;
    protected PartialResultHelper partialResultHelper;
    protected ArgumentHelper argumentHelper;
    protected EventEmitter eventEmitter;
    protected BatchLoaderHelper batchLoaderHelper;
    protected List<String> unwrapExceptions = new ArrayList<>();

    @Override
    public void initDataFetcher(Operation operation, Config config) {
        this.operation = operation;
        this.eventEmitter = EventEmitter.getInstance(config);
        this.fieldHelper = new FieldHelper(operation);
        this.reflectionHelper = new ReflectionHelper(operation, eventEmitter);
        this.argumentHelper = new ArgumentHelper(operation.getArguments());
        this.partialResultHelper = new PartialResultHelper();
        this.batchLoaderHelper = new BatchLoaderHelper();
        if (config != null && config.getUnwrapExceptions().isPresent()) {
            this.unwrapExceptions.addAll(config.getUnwrapExceptions().get());
        }
        this.unwrapExceptions.addAll(DEFAULT_EXCEPTION_UNWRAP);
    }

    @Override
    public <T> T getData(DataFetchingEnvironment dfe, DataFetcherResult.Builder<Object> resultBuilder) throws Exception {
        eventEmitter.fireBeforeDataFetch();

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
            eventEmitter.fireAfterDataFetch();
        }

        return invokeFailure(resultBuilder);
    }

    protected abstract <T> T invokeAndTransform(DataFetchingEnvironment dfe, DataFetcherResult.Builder<Object> resultBuilder,
            Object[] transformedArguments) throws AbstractDataFetcherException, Exception;

    protected abstract <T> T invokeFailure(DataFetcherResult.Builder<Object> resultBuilder);

    protected boolean shouldUnwrapThrowable(Throwable t) {
        return unwrapExceptions.contains(t.getClass().getName()) && t.getCause() != null;
    }

    protected Throwable unwrapThrowable(Throwable t) {
        if (shouldUnwrapThrowable(t)) {
            t = t.getCause();
            return unwrapThrowable(t);
        }
        return t;
    }

    protected Exception getCause(Exception t) {
        if (t.getCause() != null && t.getCause().getClass().isAssignableFrom(Exception.class)) {
            return (Exception) t.getCause();
        }
        return t;
    }

    private static List<String> DEFAULT_EXCEPTION_UNWRAP = new ArrayList<>();

    static {
        DEFAULT_EXCEPTION_UNWRAP.add(CompletionException.class.getName());
        DEFAULT_EXCEPTION_UNWRAP.add("javax.ejb.EJBException");
        DEFAULT_EXCEPTION_UNWRAP.add("jakarta.ejb.EJBException");
    }
}
