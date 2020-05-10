package io.smallrye.graphql.test.apps.async.api;

import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.GraphQLException;
import org.eclipse.microprofile.graphql.Query;
import org.eclipse.microprofile.graphql.Source;

@GraphQLApi
public class AsyncApi {

    @Query
    public CompletableFuture<AsyncSource> asyncSource() {
        return CompletableFuture.completedFuture(new AsyncSource());
    }

    @Query
    public CompletableFuture<String> asyncString(@Source AsyncSource asyncSource) {
        return CompletableFuture.completedFuture("asyncString");
    }

    @Query
    public CompletableFuture<LocalDate> asyncLocalDate(@Source AsyncSource asyncSource) {
        return CompletableFuture.completedFuture(LocalDate.parse("2006-01-02"));
    }

    @Query
    public CompletableFuture<String> asyncWithGraphQLException(@Source AsyncSource asyncSource) {
        CompletableFuture<String> future = new CompletableFuture<>();
        future.completeExceptionally(new GraphQLException("Some Exception"));
        return future;
    }

}
