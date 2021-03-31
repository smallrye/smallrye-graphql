package io.smallrye.graphql.client.dynamic.api;

import java.util.concurrent.ExecutionException;

import io.smallrye.graphql.client.Request;
import io.smallrye.graphql.client.Response;
import io.smallrye.graphql.client.core.Document;
import io.smallrye.mutiny.Uni;

public interface DynamicGraphQLClient extends AutoCloseable {

    Response executeSync(Document document) throws ExecutionException, InterruptedException;

    Response executeSync(Request request) throws ExecutionException, InterruptedException;

    Uni<Response> executeAsync(Document document);

    Uni<Response> executeAsync(Request request);

}
