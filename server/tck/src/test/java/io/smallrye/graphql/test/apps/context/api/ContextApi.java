package io.smallrye.graphql.test.apps.context.api;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import javax.inject.Inject;

import org.eclipse.microprofile.context.ThreadContext;
import org.eclipse.microprofile.graphql.DefaultValue;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;
import org.eclipse.microprofile.graphql.Source;

import io.smallrye.graphql.api.Context;

@GraphQLApi
public class ContextApi {

    @Inject
    Context context;

    @Inject
    ContextService contextService;

    @Query("context")
    public String getPathFromContext() {
        return context.getPath();
    }

    @Query("contextMethodInjected")
    public String getQueryFromContext(Context c) {
        return c.getQuery();
    }

    @Query("contextMethodInjectedAnotherIndex")
    public String getArgumentFromContext(@DefaultValue("p1") String p1, Context c, @DefaultValue("p2") String p2) {
        return (String) c.getArgument(p2);
    }

    @Query("contextFromAnotherService")
    public String getOperationType() {
        return contextService.getOperationType();
    }

    @Query("contextFromSource")
    public Pojo getPojo() {
        return new Pojo("contextFromSource");
    }

    public String path(@Source Pojo pojo) {
        return context.getPath();
    }

    @Query("contextMethodInjectedFromSource")
    public Pojo getPojo2() {
        return new Pojo("contextMethodInjectedFromSource");
    }

    public String operationType(@Source Pojo pojo, Context c) {
        return c.getOperationType();
    }

    public CompletionStage<String> asyncOperationType(@Source Pojo pojo) {
        ThreadContext threadContext = ThreadContext.builder().build();
        return threadContext.withContextCapture(
                CompletableFuture.supplyAsync(() -> contextService.getOperationType(),
                        threadContext.currentContextExecutor()));
    }
}
