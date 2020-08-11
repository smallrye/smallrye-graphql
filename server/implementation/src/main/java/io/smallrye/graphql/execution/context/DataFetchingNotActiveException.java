package io.smallrye.graphql.execution.context;

/**
 * When the context is not yet in the data fetching phase, but questions are being asked about it.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class DataFetchingNotActiveException extends RuntimeException {

    public DataFetchingNotActiveException() {
    }

    public DataFetchingNotActiveException(String message) {
        super(message);
    }

    public DataFetchingNotActiveException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataFetchingNotActiveException(Throwable cause) {
        super(cause);
    }

    public DataFetchingNotActiveException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
