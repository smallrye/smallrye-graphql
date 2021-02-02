package io.smallrye.graphql.execution.error;

import static io.smallrye.graphql.SmallRyeGraphQLServerLogging.log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionException;

import graphql.ExceptionWhileDataFetching;
import graphql.execution.DataFetcherExceptionHandler;
import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherExceptionHandlerResult;
import graphql.execution.ResultPath;
import graphql.language.SourceLocation;
import io.smallrye.graphql.bootstrap.Config;
import io.smallrye.graphql.execution.datafetcher.DataFetcherException;

/**
 * Here we have the ability to mask certain messages to the client (for security reasons)
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class ExceptionHandler implements DataFetcherExceptionHandler {

    private final Config config;
    private final ExceptionLists exceptionLists;
    protected List<String> unwrapExceptions = new ArrayList<>();

    public ExceptionHandler(Config config) {
        this.config = config;
        this.exceptionLists = new ExceptionLists(config.getHideErrorMessageList(), config.getShowErrorMessageList());
        if (config.getUnwrapExceptions().isPresent()) {
            this.unwrapExceptions.addAll(config.getUnwrapExceptions().get());
        }
        this.unwrapExceptions.addAll(DEFAULT_EXCEPTION_UNWRAP);
    }

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
            if (exceptionLists.shouldShow(throwable)) {
                return new GraphQLExceptionWhileDataFetching(path, throwable, sourceLocation);
            } else {
                return new GraphQLExceptionWhileDataFetching(config.getDefaultErrorMessage(), path, throwable, sourceLocation);
            }
        } else {
            // Check for hidelist
            if (exceptionLists.shouldHide(throwable)) {
                return new GraphQLExceptionWhileDataFetching(config.getDefaultErrorMessage(), path, throwable, sourceLocation);
            } else {
                return new GraphQLExceptionWhileDataFetching(path, throwable, sourceLocation);
            }
        }
    }

    private Throwable unwrapThrowable(Throwable t) {
        if (shouldUnwrapThrowable(t)) {
            t = t.getCause();
            return unwrapThrowable(t);
        }
        return t;
    }

    private boolean shouldUnwrapThrowable(Throwable t) {
        return unwrapExceptions.contains(t.getClass().getName()) && t.getCause() != null;
    }

    private static final List<String> DEFAULT_EXCEPTION_UNWRAP = new ArrayList<>();

    static {
        DEFAULT_EXCEPTION_UNWRAP.add(CompletionException.class.getName());
        DEFAULT_EXCEPTION_UNWRAP.add(DataFetcherException.class.getName());
        DEFAULT_EXCEPTION_UNWRAP.add("javax.ejb.EJBException");
        DEFAULT_EXCEPTION_UNWRAP.add("jakarta.ejb.EJBException");
    }
}
