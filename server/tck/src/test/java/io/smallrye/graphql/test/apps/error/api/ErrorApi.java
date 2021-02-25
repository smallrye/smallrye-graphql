package io.smallrye.graphql.test.apps.error.api;

import java.security.AccessControlException;
import java.util.concurrent.CompletableFuture;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

import io.smallrye.graphql.api.ErrorCode;
import io.smallrye.mutiny.Uni;

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

    @Query
    public CompletableFuture<String> annotatedAsyncCustomBusinessException() {
        return CompletableFuture.supplyAsync(() -> {
            throw new AnnotatedCustomBusinessException();
        });
    }

    @Query
    public Uni<String> annotatedReactiveCustomBusinessException() {
        return Uni.createFrom().failure(new AnnotatedCustomBusinessException());
    }

    @Query
    public String securityException() {
        throw new AccessControlException("User is not authorized");
    }
}
