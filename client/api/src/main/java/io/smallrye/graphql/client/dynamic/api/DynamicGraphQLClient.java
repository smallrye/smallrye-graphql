package io.smallrye.graphql.client.dynamic.api;

import java.util.concurrent.ExecutionException;

import io.smallrye.graphql.client.mpapi.Request;
import io.smallrye.graphql.client.mpapi.Response;
import io.smallrye.graphql.client.mpapi.core.Document;
import io.smallrye.mutiny.Uni;

public interface DynamicGraphQLClient extends AutoCloseable {

    Response executeSync(Document document) throws ExecutionException, InterruptedException;

    Response executeSync(Request request) throws ExecutionException, InterruptedException;

    Uni<Response> executeAsync(Document document);

    Uni<Response> executeAsync(Request request);

}
