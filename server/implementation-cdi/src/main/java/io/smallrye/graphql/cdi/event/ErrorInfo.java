package io.smallrye.graphql.cdi.event;

/**
 * Simple Pojo that hold error info
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class ErrorInfo {
    private String executionId;
    private Throwable t;

    public ErrorInfo(String executionId, Throwable t) {
        this.executionId = executionId;
        this.t = t;
    }

    public String getExecutionId() {
        return executionId;
    }

    public Throwable getT() {
        return t;
    }
}
