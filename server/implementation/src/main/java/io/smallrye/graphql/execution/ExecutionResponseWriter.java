package io.smallrye.graphql.execution;

/**
 * Write the response to something
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public interface ExecutionResponseWriter {

    public void write(ExecutionResponse er);

    default void fail(Throwable t) {
        if (t.getClass().isAssignableFrom(RuntimeException.class)) {
            throw (RuntimeException) t;
        } else {
            throw new RuntimeException(t);
        }
    }
}
