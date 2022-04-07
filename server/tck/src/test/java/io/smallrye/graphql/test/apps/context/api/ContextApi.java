package io.smallrye.graphql.test.apps.context.api;

import java.io.IOException;
import java.io.StringWriter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonWriter;

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
        k.setSelectedFieldIncludingSource(toString(context.getSelectedFields(true)));
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

    private String toString(JsonArray jsonArray) {
        try (StringWriter sw = new StringWriter();
                JsonWriter writer = Json.createWriter(sw)) {
            writer.writeArray(jsonArray);
            writer.close();
            return sw.toString();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
