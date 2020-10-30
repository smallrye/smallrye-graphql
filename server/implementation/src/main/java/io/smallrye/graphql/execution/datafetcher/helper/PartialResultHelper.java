package io.smallrye.graphql.execution.datafetcher.helper;

import org.eclipse.microprofile.graphql.GraphQLException;

import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherResult;
import graphql.execution.ExecutionPath;
import graphql.language.SourceLocation;
import graphql.schema.DataFetchingEnvironment;
import io.smallrye.graphql.execution.error.GraphQLExceptionWhileDataFetching;

/**
 * Helping with PartialResults
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class PartialResultHelper {

    public DataFetcherResult.Builder<Object> appendPartialResult(
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
