package io.smallrye.graphql.client.dynamic.api;

import java.util.Map;
import java.util.concurrent.ExecutionException;

import io.smallrye.graphql.client.Request;
import io.smallrye.graphql.client.Response;
import io.smallrye.graphql.client.core.Document;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

public interface DynamicGraphQLClient extends AutoCloseable {

    Response executeSync(Document document) throws ExecutionException, InterruptedException;

    Response executeSync(Document document, Map<String, Object> variables) throws ExecutionException, InterruptedException;

    Response executeSync(Document document, String operationName) throws ExecutionException, InterruptedException;

    Response executeSync(Document document, Map<String, Object> variables, String operationName)
            throws ExecutionException, InterruptedException;

    Response executeSync(Request request) throws ExecutionException, InterruptedException;

    Response executeSync(String query) throws ExecutionException, InterruptedException;

    Response executeSync(String query, Map<String, Object> variables) throws ExecutionException, InterruptedException;

    Response executeSync(String query, String operationName) throws ExecutionException, InterruptedException;

    Response executeSync(String query, Map<String, Object> variables, String operationName)
            throws ExecutionException, InterruptedException;

    Uni<Response> executeAsync(Document document);

    Uni<Response> executeAsync(Document document, Map<String, Object> variables);

    Uni<Response> executeAsync(Document document, String operationName);

    Uni<Response> executeAsync(Document document, Map<String, Object> variables, String operationName);

    Uni<Response> executeAsync(Request request);

    Uni<Response> executeAsync(String query);

    Uni<Response> executeAsync(String query, Map<String, Object> variables);

    Uni<Response> executeAsync(String query, String operationName);

    Uni<Response> executeAsync(String query, Map<String, Object> variables, String operationName);

    Multi<Response> subscription(Document document);

    Multi<Response> subscription(Document document, Map<String, Object> variables);

    Multi<Response> subscription(Document document, String operationName);

    Multi<Response> subscription(Document document, Map<String, Object> variables, String operationName);

    Multi<Response> subscription(Request request);

    Multi<Response> subscription(String query);

    Multi<Response> subscription(String query, Map<String, Object> variables);

    Multi<Response> subscription(String query, String operationName);

    Multi<Response> subscription(String query, Map<String, Object> variables, String operationName);

}
