package io.smallrye.graphql.execution.datafetcher.helper;

import java.util.List;

import org.eclipse.microprofile.graphql.GraphQLException;

import graphql.GraphQLError;
import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherExceptionHandlerResult;
import graphql.execution.DataFetcherResult;
import graphql.execution.ResultPath;
import graphql.language.SourceLocation;
import graphql.schema.DataFetchingEnvironment;
import io.smallrye.graphql.execution.error.ExceptionHandler;
import io.smallrye.graphql.execution.error.GraphQLExceptionWhileDataFetching;

/**
 * Helping with PartialResults
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class ErrorResultHelper {

    private final ExceptionHandler exceptionHandler = new ExceptionHandler();

    public void appendPartialResult(
            DataFetcherResult.Builder<Object> resultBuilder,
            DataFetchingEnvironment dfe,
            GraphQLException graphQLException) {

        DataFetcherExceptionHandlerParameters handlerParameters = DataFetcherExceptionHandlerParameters
                .newExceptionParameters()
                .dataFetchingEnvironment(dfe)
                .exception(graphQLException)
                .build();

        SourceLocation sourceLocation = handlerParameters.getSourceLocation();
        ResultPath path = handlerParameters.getPath();
        GraphQLExceptionWhileDataFetching error = new GraphQLExceptionWhileDataFetching(path, graphQLException,
                sourceLocation);

        resultBuilder
                .data(graphQLException.getPartialResults())
                .error(error);
    }

    public List<GraphQLError> toGraphQLErrors(DataFetchingEnvironment dfe, Throwable t) {
        DataFetcherExceptionHandlerParameters handlerParameters = DataFetcherExceptionHandlerParameters
                .newExceptionParameters()
                .dataFetchingEnvironment(dfe)
                .exception(t)
                .build();

        DataFetcherExceptionHandlerResult exceptionHandlerResult = null;
        try {
            exceptionHandlerResult = exceptionHandler.handleException(handlerParameters).get();
        } catch (Exception e) {
            // this should generally not happen - exceptionHandler.handleException doesn't do any IO and doesn't
            // throw exceptions, so maybe only if we get interrupted at the right moment
            throw new RuntimeException(e);
        }

        return exceptionHandlerResult.getErrors();
    }

    public void appendException(
            DataFetcherResult.Builder<Object> resultBuilder,
            DataFetchingEnvironment dfe,
            Throwable t) {

        resultBuilder
                .errors(toGraphQLErrors(dfe, t));
    }
}
