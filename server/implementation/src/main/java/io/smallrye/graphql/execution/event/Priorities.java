package io.smallrye.graphql.execution.event;

/**
 * Constants used in combination with {@code javax.annotation.Priority} to control the invocation order of
 * {@code EventingService}s in {@code EventEmitter}
 */
public class Priorities {
    public static final int FIRST_IN_LAST_OUT = 0;
    public static final int DEFAULT = 1000;
    public static final int LAST_IN_FIRST_OUT = 2000;
}
