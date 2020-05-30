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
import io.smallrye.graphql.SmallRyeGraphQLServerMessages;
import io.smallrye.graphql.execution.datafetcher.decorator.DataFetcherDecorator;
import io.smallrye.graphql.schema.model.Operation;
import io.smallrye.graphql.transformation.DataFetchingException;

/**
 * Fetch data from resolvers that return {@code CompletionStage}.
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
     *
     * <ol>
     * <li>Get the correct instance of the class we want to make the call in using CDI. That allow the developer to still use
     * Scopes in the bean.
     * <li>Get the argument values (if any) from graphql-java and make sue they are in the correct type, and if needed,
     * transformed.
     * <li>Make a call on the method with the correct arguments ang get the result.
     * <li>transform the result (which is wrapped in {@code CompletionStage}, and wrap it in
     * {@code CompletionStage<DataFetcherResult>}.
     * </ol>
     *
     * @param dfe the Data Fetching Environment from graphql-java
     * @return the result from the call, wrapped in an (possibly asnyc completing) {@link CompletionStage}
     * @throws Exception if the resolver throws an exception
     * @throws DataFetcherException if the reflective call fails
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
                        throw SmallRyeGraphQLServerMessages.msg.dataFetcherException(operation, throwable);
                    } else if (throwable instanceof Error) {
                        throw ((Error) throwable);
                    }
                } else {
                    try {
                        resultBuilder.data(fieldHelper.transformResponse(result));
                    } catch (DataFetchingException te) {
                        te.appendDataFetcherResult(resultBuilder, dfe);
                    }
                }

                return resultBuilder.build();
            });

        } catch (DataFetchingException pe) {
            //Arguments or result couldn't be transformed
            pe.appendDataFetcherResult(resultBuilder, dfe);
        } catch (GraphQLException graphQLException) {
            appendPartialResult(resultBuilder, dfe, graphQLException);
        } catch (SecurityException | IllegalAccessException | IllegalArgumentException ex) {
            //m.invoke failed
            throw SmallRyeGraphQLServerMessages.msg.dataFetcherException(operation, ex);
        }

        return CompletableFuture.completedFuture(resultBuilder.build());
    }

}
