package io.smallrye.graphql.execution.error;

import graphql.ExceptionWhileDataFetching;
import graphql.execution.DataFetcherExceptionHandler;
import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherExceptionHandlerResult;
import graphql.execution.ExecutionPath;
import graphql.language.SourceLocation;
import io.smallrye.graphql.SmallRyeGraphQLServerLogging;
import io.smallrye.graphql.bootstrap.Config;

/**
 * Here we have the ability to mask certain messages to the client (for security reasons)
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class ExceptionHandler implements DataFetcherExceptionHandler {

    private final Config config;
    private final ExceptionLists exceptionLists;

    public ExceptionHandler(Config config) {
        this.config = config;
        this.exceptionLists = new ExceptionLists(config.getBlackList(), config.getWhiteList());
    }

    @Override
    public DataFetcherExceptionHandlerResult onException(DataFetcherExceptionHandlerParameters handlerParameters) {
        Throwable throwable = handlerParameters.getException();
        SourceLocation sourceLocation = handlerParameters.getSourceLocation();
        ExecutionPath path = handlerParameters.getPath();
        ExceptionWhileDataFetching error = getExceptionWhileDataFetching(throwable, sourceLocation, path);

        if (config.isPrintDataFetcherException()) {
            SmallRyeGraphQLServerLogging.log.dataFetchingError(throwable);
        }

        return DataFetcherExceptionHandlerResult.newResult().error(error).build();
    }

    private ExceptionWhileDataFetching getExceptionWhileDataFetching(Throwable throwable, SourceLocation sourceLocation,
            ExecutionPath path) {
        if (throwable instanceof RuntimeException) {
            // Check for whitelist
            if (exceptionLists.isWhitelisted(throwable)) {
                return new GraphQLExceptionWhileDataFetching(path, throwable, sourceLocation);
            } else {
                return new GraphQLExceptionWhileDataFetching(config.getDefaultErrorMessage(), path, throwable, sourceLocation);
            }
        } else {
            // Check for blacklist
            if (exceptionLists.isBlacklisted(throwable)) {
                return new GraphQLExceptionWhileDataFetching(config.getDefaultErrorMessage(), path, throwable, sourceLocation);
            } else {
                return new GraphQLExceptionWhileDataFetching(path, throwable, sourceLocation);
            }
        }
    }
}
