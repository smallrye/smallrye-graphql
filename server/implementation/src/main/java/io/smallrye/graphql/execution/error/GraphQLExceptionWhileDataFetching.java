package io.smallrye.graphql.execution.error;

import graphql.ExceptionWhileDataFetching;
import graphql.GraphqlErrorHelper;
import graphql.execution.ResultPath;
import graphql.language.SourceLocation;

/**
 * Simple way to override the message to only use the original exception message
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class GraphQLExceptionWhileDataFetching extends ExceptionWhileDataFetching {

    private final String message;

    public GraphQLExceptionWhileDataFetching(ResultPath path, Throwable exception, SourceLocation sourceLocation) {
        super(path, exception, sourceLocation);
        this.message = super.getException().getLocalizedMessage();
    }

    public GraphQLExceptionWhileDataFetching(String message, ResultPath path, Throwable exception,
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
