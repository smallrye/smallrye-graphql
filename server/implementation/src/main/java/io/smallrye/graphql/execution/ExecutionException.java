package io.smallrye.graphql.execution;

/**
 * Error while executing a request
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class ExecutionException extends RuntimeException {

    public ExecutionException() {
    }

    public ExecutionException(String string) {
        super(string);
    }

    public ExecutionException(String string, Throwable thrwbl) {
        super(string, thrwbl);
    }

    public ExecutionException(Throwable thrwbl) {
        super(thrwbl);
    }

    public ExecutionException(String string, Throwable thrwbl, boolean bln, boolean bln1) {
        super(string, thrwbl, bln, bln1);
    }

}
