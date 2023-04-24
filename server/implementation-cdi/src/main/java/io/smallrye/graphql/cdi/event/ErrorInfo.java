package io.smallrye.graphql.cdi.event;

import io.smallrye.graphql.api.Context;

/**
 * Simple Pojo that hold error info
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class ErrorInfo {
    private final Context context;
    private final Throwable t;

    ErrorInfo(Context context, Throwable t) {
        this.context = context;
        this.t = t;
    }

    public Context getContext() {
        return context;
    }

    public String getExecutionId() {
        return context.getExecutionId();
    }

    public Throwable getT() {
        return t;
    }
}
