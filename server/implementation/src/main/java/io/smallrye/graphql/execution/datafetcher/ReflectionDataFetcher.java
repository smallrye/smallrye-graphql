package io.smallrye.graphql.execution.datafetcher;

import static io.smallrye.graphql.SmallRyeGraphQLServerMessages.msg;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

import org.eclipse.microprofile.graphql.GraphQLException;

import graphql.GraphQLContext;
import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherResult;
import graphql.execution.ExecutionPath;
import graphql.language.SourceLocation;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import io.smallrye.graphql.bootstrap.Config;
import io.smallrye.graphql.execution.context.SmallRyeContext;
import io.smallrye.graphql.execution.datafetcher.helper.ArgumentHelper;
import io.smallrye.graphql.execution.datafetcher.helper.FieldHelper;
import io.smallrye.graphql.execution.datafetcher.helper.ReflectionHelper;
import io.smallrye.graphql.execution.error.GraphQLExceptionWhileDataFetching;
import io.smallrye.graphql.execution.event.EventEmitter;
import io.smallrye.graphql.schema.model.Operation;
import io.smallrye.graphql.transformation.AbstractDataFetcherException;

/**
 * Fetch data using some bean lookup and Reflection
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class ReflectionDataFetcher<T> implements DataFetcher<T> {

    private final Config config;
    private final Operation operation;
    private final FieldHelper fieldHelper;
    private final ReflectionHelper reflectionHelper;
    private final ArgumentHelper argumentHelper;

    /**
     * We use this reflection data fetcher on operations (so Queries, Mutations and Source)
     *
     * ParameterClasses: We need an Array of Classes that this operation method needs so we can use reflection to call the
     * method.
     * FieldHelper: We might have to transform the data on the way out if there was a Formatting annotation on the method,
     * or return object fields, we can not use normal JsonB to do this because we do not bind a full object, and we support
     * annotation that is not part on JsonB
     *
     * ArgumentHelper: The same as above, except for every parameter on the way in.
     *
     * @param operation the operation
     *
     */
    public ReflectionDataFetcher(Config config, Operation operation) {
        this.config = config;
        this.operation = operation;
        this.fieldHelper = new FieldHelper(operation);
        this.reflectionHelper = new ReflectionHelper(operation);
        this.argumentHelper = new ArgumentHelper(operation.getArguments());
    }

    @Override
    public T get(final DataFetchingEnvironment dfe) throws Exception {
        SmallRyeContext.setDataFromFetcher(dfe, operation);

        final GraphQLContext context = dfe.getContext();
        final DataFetcherResult.Builder<Object> resultBuilder = DataFetcherResult.newResult().localContext(context);

        EventEmitter.fireBeforeDataFetch();

        if (operation.isAsync()) {
            return (T) getAsync(dfe, resultBuilder);
        } else {
            return (T) getSync(dfe, resultBuilder);
        }
    }

    private DataFetcherResult<Object> getSync(final DataFetchingEnvironment dfe,
            final DataFetcherResult.Builder<Object> resultBuilder) throws Exception {

        try {
            Object[] transformedArguments = argumentHelper.getArguments(dfe);
            Object resultFromMethodCall = reflectionHelper.invoke(transformedArguments);
            Object resultFromTransform = fieldHelper.transformResponse(resultFromMethodCall);
            resultBuilder.data(resultFromTransform);
        } catch (AbstractDataFetcherException abstractDataFetcherException) {
            //Arguments or result couldn't be transformed
            abstractDataFetcherException.appendDataFetcherResult(resultBuilder, dfe);
            EventEmitter.fireOnDataFetchError(dfe.getExecutionId().toString(), abstractDataFetcherException);
        } catch (GraphQLException graphQLException) {
            appendPartialResult(resultBuilder, dfe, graphQLException);
            EventEmitter.fireOnDataFetchError(dfe.getExecutionId().toString(), graphQLException);
        } catch (SecurityException | IllegalAccessException | IllegalArgumentException ex) {
            //m.invoke failed
            EventEmitter.fireOnDataFetchError(dfe.getExecutionId().toString(), ex);
            throw msg.dataFetcherException(operation, ex);
        } finally {
            EventEmitter.fireAfterDataFetch();
        }

        return resultBuilder.build();
    }

    private CompletionStage<DataFetcherResult<Object>> getAsync(final DataFetchingEnvironment dfe,
            final DataFetcherResult.Builder<Object> resultBuilder) throws Exception {

        try {
            Object[] transformedArguments = argumentHelper.getArguments(dfe);
            CompletionStage<Object> futureResultFromMethodCall = reflectionHelper.invoke(transformedArguments);

            return futureResultFromMethodCall.handle((result, throwable) -> {
                if (throwable instanceof CompletionException) {
                    //Exception thrown by underlying method may be wrapped in CompletionException
                    throwable = throwable.getCause();
                }

                if (throwable != null) {
                    EventEmitter.fireOnDataFetchError(dfe.getExecutionId().toString(), throwable);
                    if (throwable instanceof GraphQLException) {
                        GraphQLException graphQLException = (GraphQLException) throwable;
                        appendPartialResult(resultBuilder, dfe, graphQLException);
                    } else if (throwable instanceof Exception) {
                        throw msg.dataFetcherException(operation, throwable);
                    } else if (throwable instanceof Error) {
                        throw ((Error) throwable);
                    }
                } else {
                    try {
                        resultBuilder.data(fieldHelper.transformResponse(result));
                    } catch (AbstractDataFetcherException te) {
                        te.appendDataFetcherResult(resultBuilder, dfe);
                    }
                }

                return resultBuilder.build();
            });

        } catch (AbstractDataFetcherException abstractDataFetcherException) {
            //Arguments or result couldn't be transformed
            abstractDataFetcherException.appendDataFetcherResult(resultBuilder, dfe);
            EventEmitter.fireOnDataFetchError(dfe.getExecutionId().toString(), abstractDataFetcherException);
        } catch (GraphQLException graphQLException) {
            appendPartialResult(resultBuilder, dfe, graphQLException);
            EventEmitter.fireOnDataFetchError(dfe.getExecutionId().toString(), graphQLException);
        } catch (SecurityException | IllegalAccessException | IllegalArgumentException ex) {
            //m.invoke failed
            EventEmitter.fireOnDataFetchError(dfe.getExecutionId().toString(), ex);
            throw msg.dataFetcherException(operation, ex);
        } finally {
            EventEmitter.fireAfterDataFetch();
        }

        return CompletableFuture.completedFuture(resultBuilder.build());

    }

    private DataFetcherResult.Builder<Object> appendPartialResult(
            DataFetcherResult.Builder<Object> resultBuilder,
            DataFetchingEnvironment dfe,
            GraphQLException graphQLException) {
        DataFetcherExceptionHandlerParameters handlerParameters = DataFetcherExceptionHandlerParameters
                .newExceptionParameters()
                .dataFetchingEnvironment(dfe)
                .exception(graphQLException)
                .build();

        SourceLocation sourceLocation = handlerParameters.getSourceLocation();
        ExecutionPath path = handlerParameters.getPath();
        GraphQLExceptionWhileDataFetching error = new GraphQLExceptionWhileDataFetching(path, graphQLException,
                sourceLocation);

        return resultBuilder
                .data(graphQLException.getPartialResults())
                .error(error);
    }
}
