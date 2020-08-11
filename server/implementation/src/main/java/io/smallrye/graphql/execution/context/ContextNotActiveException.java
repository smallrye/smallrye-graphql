package io.smallrye.graphql.execution.context;

public class ContextNotActiveException extends RuntimeException {

    public ContextNotActiveException() {
    }

    public ContextNotActiveException(String message) {
        super(message);
    }

    public ContextNotActiveException(String message, Throwable cause) {
        super(message, cause);
    }

    public ContextNotActiveException(Throwable cause) {
        super(cause);
    }

    public ContextNotActiveException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
