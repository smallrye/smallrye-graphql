package io.smallrye.graphql.test.apps.async.api;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.microprofile.graphql.DateFormat;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.GraphQLException;
import org.eclipse.microprofile.graphql.NonNull;
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
    public CompletableFuture<List<Integer>> asyncList(@Source AsyncSource asyncSource) {
        return CompletableFuture.completedFuture(Arrays.asList(1, 2, 3));
    }

    @Query
    public CompletableFuture<LocalDate> asyncLocalDate(@Source AsyncSource asyncSource) {
        return CompletableFuture.completedFuture(LocalDate.parse("2006-01-02"));
    }

    @Query
    @DateFormat("MM/dd/yyyy")
    public CompletableFuture<LocalDate> asyncFormattedLocalDate(@Source AsyncSource asyncSource) {
        return CompletableFuture.completedFuture(LocalDate.parse("2006-01-02"));
    }

    @Query
    @NonNull
    public CompletableFuture<String> asyncNonNullString(@Source AsyncSource asyncSource) {
        return CompletableFuture.completedFuture("asyncNonNullString");
    }

    @Query
    public CompletableFuture<String> asyncWithGraphQLException(@Source AsyncSource asyncSource) {
        CompletableFuture<String> future = new CompletableFuture<>();
        future.completeExceptionally(new GraphQLException("Some Exception"));
        return future;
    }

}
