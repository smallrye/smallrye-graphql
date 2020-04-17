package io.smallrye.graphql.execution.error;

import graphql.ExceptionWhileDataFetching;
import graphql.GraphqlErrorHelper;
import graphql.execution.ExecutionPath;
import graphql.language.SourceLocation;

/**
 * Simple way to override the message to only use the original exception message
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class GraphQLExceptionWhileDataFetching extends ExceptionWhileDataFetching {

    private final String message;

    public GraphQLExceptionWhileDataFetching(ExecutionPath path, Throwable exception, SourceLocation sourceLocation) {
        super(path, exception, sourceLocation);
        this.message = super.getException().getMessage();
    }

    public GraphQLExceptionWhileDataFetching(String message, ExecutionPath path, Throwable exception,
            SourceLocation sourceLocation) {
        super(path, exception, sourceLocation);
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override
    public boolean equals(Object o) {
        return GraphqlErrorHelper.equals(this, o);
    }

    @Override
    public int hashCode() {
        return GraphqlErrorHelper.hashCode(this);
    }
}
