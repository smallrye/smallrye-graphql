package io.smallrye.graphql.execution.datafetcher;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

import org.eclipse.microprofile.graphql.GraphQLException;

import graphql.GraphQLContext;
import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetchingEnvironment;
import io.smallrye.graphql.execution.datafetcher.decorator.DataFetcherDecorator;
import io.smallrye.graphql.schema.model.Operation;
import io.smallrye.graphql.transformation.TransformException;

/**
 * Fetch data using some bean lookup and Reflection
 *
 * @author Yannick Bröker (ybroeker@techfak.uni-bielefeld.de)
 */
public class AsyncDataFetcher extends AbstractDataFetcher<CompletionStage<DataFetcherResult<Object>>> {

    public AsyncDataFetcher(Operation operation) {
        this(operation, Collections.emptyList());
    }

    public AsyncDataFetcher(Operation operation, Collection<DataFetcherDecorator> decorators) {
        super(operation, decorators);
    }

    /**
     * This makes the call on the method. We do the following:
     * 1) Get the correct instance of the class we want to make the call in using CDI. That allow the developer to still use
     * Scopes in the bean.
     * 2) Get the argument values (if any) from graphql-java and make sue they are in the correct type, and if needed,
     * transformed.
     * 3) Make a call on the method with the correct arguments
     * 4) get the result and if needed transform it before we return it.
     *
     * @param dfe the Data Fetching Environment from graphql-java
     * @return the result from the call.
     * @throws Exception
     */
    @Override
    public CompletionStage<DataFetcherResult<Object>> get(DataFetchingEnvironment dfe) throws Exception {
        //TODO: custom context object?
        final GraphQLContext context = GraphQLContext.newContext().build();
        final DataFetcherResult.Builder<Object> resultBuilder = DataFetcherResult.newResult().localContext(context);

        Class<?> operationClass = classloadingService.loadClass(operation.getClassName());
        Method m = getMethod(operationClass);

        Object declaringObject = lookupService.getInstance(operationClass);

        try {
            Object[] transformedArguments = argumentHelper.getArguments(dfe);
            ExecutionContextImpl executionContext = new ExecutionContextImpl(declaringObject, m, transformedArguments, context,
                    dfe,
                    decorators.iterator());

            CompletionStage<Object> futureResult = execute(executionContext);

            return futureResult.handle((result, throwable) -> {
                if (throwable instanceof CompletionException) {
                    //Exception thrown by underlying method may be wrapped in CompletionException
                    throwable = throwable.getCause();
                }

                if (throwable != null) {
                    if (throwable instanceof GraphQLException) {
                        GraphQLException graphQLException = (GraphQLException) throwable;
                        appendPartialResult(resultBuilder, dfe, graphQLException);
                    } else if (throwable instanceof Exception) {
                        throw new DataFetcherException(operation, (Exception) throwable);
                    } else if (throwable instanceof Error) {
                        throw ((Error) throwable);
                    }
                } else {
                    try {
                        resultBuilder.data(fieldHelper.transformResponse(result));
                    } catch (TransformException te) {
                        te.appendDataFetcherResult(resultBuilder, dfe);
                    }
                }

                return resultBuilder.build();
            });

        } catch (TransformException pe) {
            //Arguments or result couldn't be transformed
            pe.appendDataFetcherResult(resultBuilder, dfe);
        } catch (GraphQLException graphQLException) {
            appendPartialResult(resultBuilder, dfe, graphQLException);
        } catch (SecurityException | IllegalAccessException | IllegalArgumentException ex) {
            //m.invoke failed
            throw new DataFetcherException(operation, ex);
        }

        return CompletableFuture.completedFuture(resultBuilder.build());
    }

}
