package io.smallrye.graphql.test.apps.error.api;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

import io.smallrye.graphql.api.ErrorCode;

@GraphQLApi
public class ErrorApi {
    @Query
    public String unsupportedOperation() {
        throw new UnsupportedOperationException("dummy-message");
    }

    public static class CustomBusinessException extends RuntimeException {
    }

    @Query
    public String customBusinessException() {
        throw new CustomBusinessException();
    }

    @ErrorCode("some-business-error-code")
    public static class AnnotatedCustomBusinessException extends RuntimeException {
    }

    @Query
    public String annotatedCustomBusinessException() {
        throw new AnnotatedCustomBusinessException();
    }
}
