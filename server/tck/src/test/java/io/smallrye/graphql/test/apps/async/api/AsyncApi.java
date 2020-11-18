package io.smallrye.graphql.test.apps.async.api;

import java.time.LocalDate;
import java.util.ArrayList;
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

    // Normal Async Query
    @Query
    public CompletableFuture<AsyncSource> asyncSource() {
        return CompletableFuture.completedFuture(new AsyncSource());
    }

    // Normal Async Source
    public CompletableFuture<String> asyncString(@Source AsyncSource asyncSource) {
        return CompletableFuture.completedFuture("asyncString");
    }

    // Normal Async Source List
    public CompletableFuture<List<Integer>> asyncList(@Source AsyncSource asyncSource) {
        return CompletableFuture.completedFuture(Arrays.asList(1, 2, 3));
    }

    // Normal Async Source Date
    public CompletableFuture<LocalDate> asyncLocalDate(@Source AsyncSource asyncSource) {
        return CompletableFuture.completedFuture(LocalDate.parse("2006-01-02"));
    }

    // Normal Async Source Date with formatting
    @DateFormat("MM/dd/yyyy")
    public CompletableFuture<LocalDate> asyncFormattedLocalDate(@Source AsyncSource asyncSource) {
        return CompletableFuture.completedFuture(LocalDate.parse("2006-01-02"));
    }

    // Normal Async not null Source
    @NonNull
    public CompletableFuture<String> asyncNonNullString(@Source AsyncSource asyncSource) {
        return CompletableFuture.completedFuture("asyncNonNullString");
    }

    // Normal Async Source with Exception
    public CompletableFuture<String> asyncWithGraphQLException(@Source AsyncSource asyncSource) {
        CompletableFuture<String> future = new CompletableFuture<>();
        future.completeExceptionally(new GraphQLException("Some Exception"));
        return future;
    }

    // Batch Async Query
    @Query
    public CompletableFuture<List<AsyncSource>> asyncSources() {
        List<AsyncSource> l = new ArrayList<>();
        l.add(new AsyncSource());
        l.add(new AsyncSource());

        return CompletableFuture.completedFuture(l);
    }

    // Batch Async Source
    public CompletableFuture<List<String>> asyncStrings(@Source List<AsyncSource> asyncSources) {
        int cnt = 0;
        List<String> s = new ArrayList<>();
        for (AsyncSource as : asyncSources) {
            s.add(as.getSimpleField() + ++cnt);
        }
        return CompletableFuture.completedFuture(s);
    }

    // Batch Async Source List
    public CompletableFuture<List<List<Integer>>> asyncListBatch(@Source List<AsyncSource> asyncSource) {
        List<List<Integer>> fl = new ArrayList<>();
        for (AsyncSource as : asyncSource) {
            List<Integer> il = Arrays.asList(1, 2, 3);
            fl.add(il);
        }
        return CompletableFuture.completedFuture(fl);
    }
}
