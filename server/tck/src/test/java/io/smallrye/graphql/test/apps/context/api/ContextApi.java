package io.smallrye.graphql.test.apps.context.api;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import jakarta.inject.Inject;

import org.eclipse.microprofile.context.ThreadContext;
import org.eclipse.microprofile.graphql.DefaultValue;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;
import org.eclipse.microprofile.graphql.Source;

import io.smallrye.graphql.api.Context;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;

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
        return c.getArgument(p2);
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

    @Query
    public Konteks getKonteks() {
        Konteks k = new Konteks();

        k.setPath(context.getPath());
        k.setFieldName(context.getFieldName());
        k.setOperationType(context.getOperationType());
        k.setSelectedFields(toString(context.getSelectedFields()));
        k.setSelectedFieldIncludingSource(toString(context.getSelectedAndSourceFields()));
        k.setRequestedOperationTypes(context.getRequestedOperationTypes());
        return k;
    }

    public String operationType(@Source Pojo pojo, Context c) {
        return c.getOperationType();
    }

    public String sourceOnKonteks(@Source Konteks k) {
        return "source";
    }

    public CompletionStage<String> asyncOperationType(@Source Pojo pojo) {
        ThreadContext threadContext = ThreadContext.builder().build();
        return threadContext.withContextCapture(
                CompletableFuture.supplyAsync(() -> contextService.getOperationType(),
                        threadContext.currentContextExecutor()));
    }

    private String toString(ArrayNode arrayNode) {
        try {
            return new ObjectMapper().writeValueAsString(arrayNode);
        } catch (tools.jackson.core.JacksonException e) {
            throw new RuntimeException(e);
        }
    }
}
