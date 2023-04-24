package io.smallrye.graphql.execution.error;

import static io.smallrye.graphql.SmallRyeGraphQLServerLogging.log;

import graphql.ExceptionWhileDataFetching;
import graphql.execution.DataFetcherExceptionHandler;
import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherExceptionHandlerResult;
import graphql.execution.ResultPath;
import graphql.language.SourceLocation;
import io.smallrye.graphql.spi.config.Config;

/**
 * Here we have the ability to mask certain messages to the client (for security reasons)
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class ExceptionHandler implements DataFetcherExceptionHandler {

    private final Config config = Config.get();

    @Override
    public DataFetcherExceptionHandlerResult onException(DataFetcherExceptionHandlerParameters handlerParameters) {
        Throwable throwable = handlerParameters.getException();
        throwable = unwrapThrowable(throwable);
        SourceLocation sourceLocation = handlerParameters.getSourceLocation();
        ResultPath path = handlerParameters.getPath();
        ExceptionWhileDataFetching error = getExceptionWhileDataFetching(throwable, sourceLocation, path);
        if (config.isPrintDataFetcherException()) {
            log.dataFetchingError(throwable);
        }

        return DataFetcherExceptionHandlerResult.newResult().error(error).build();
    }

    private ExceptionWhileDataFetching getExceptionWhileDataFetching(Throwable throwable, SourceLocation sourceLocation,
            ResultPath path) {
        if (throwable instanceof RuntimeException) {
            // Check for showlist
            if (config.shouldShow(throwable)) {
                return new GraphQLExceptionWhileDataFetching(path, throwable, sourceLocation);
            } else {
                return new GraphQLExceptionWhileDataFetching(config.getDefaultErrorMessage(), path,
                        throwable,
                        sourceLocation);
            }
        } else {
            // Check for hidelist
            if (config.shouldHide(throwable)) {
                return new GraphQLExceptionWhileDataFetching(config.getDefaultErrorMessage(), path,
                        throwable,
                        sourceLocation);
            } else {
                return new GraphQLExceptionWhileDataFetching(path, throwable, sourceLocation);
            }
        }
    }

    private Throwable unwrapThrowable(Throwable t) {
        if (config.shouldUnwrapThrowable(t)) {
            t = t.getCause();
            return unwrapThrowable(t);
        }
        return t;
    }
}
