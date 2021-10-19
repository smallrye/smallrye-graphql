package io.smallrye.graphql.client.dynamic.vertx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import io.smallrye.graphql.client.Request;
import io.smallrye.graphql.client.Response;
import io.smallrye.graphql.client.ResponseReader;
import io.smallrye.graphql.client.core.Document;
import io.smallrye.graphql.client.dynamic.RequestImpl;
import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClient;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.WebSocket;
import io.vertx.core.http.WebsocketVersion;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;

public class VertxDynamicGraphQLClient implements DynamicGraphQLClient {

    private final WebClient webClient;
    private final HttpClient httpClient;
    private final String url;
    private final MultiMap headers;

    VertxDynamicGraphQLClient(Vertx vertx, String url, MultiMap headers, WebClientOptions options) {
        if (options != null) {
            this.httpClient = vertx.createHttpClient(options);
        } else {
            this.httpClient = vertx.createHttpClient();
        }
        this.webClient = WebClient.wrap(httpClient);
        this.headers = headers;
        this.url = url;
    }

    @Override
    public Response executeSync(Document document) throws ExecutionException, InterruptedException {
        return executeSync(buildRequest(document, null, null));
    }

    @Override
    public Response executeSync(Document document, Map<String, Object> variables)
            throws ExecutionException, InterruptedException {
        return executeSync(buildRequest(document, variables, null));
    }

    @Override
    public Response executeSync(Document document, String operationName) throws ExecutionException, InterruptedException {
        return executeSync(buildRequest(document, null, operationName));
    }

    @Override
    public Response executeSync(Document document, Map<String, Object> variables, String operationName)
            throws ExecutionException, InterruptedException {
        return executeSync(buildRequest(document, variables, operationName));
    }

    @Override
    public Response executeSync(Request request) throws ExecutionException, InterruptedException {
        return executeSync(Buffer.buffer(request.toJson()));
    }

    @Override
    public Response executeSync(String query) throws ExecutionException, InterruptedException {
        return executeSync(Buffer.buffer(buildRequest(query, null, null).toJson()));
    }

    @Override
    public Response executeSync(String query, Map<String, Object> variables) throws ExecutionException, InterruptedException {
        return executeSync(Buffer.buffer(buildRequest(query, variables, null).toJson()));
    }

    @Override
    public Response executeSync(String query, String operationName) throws ExecutionException, InterruptedException {
        return executeSync(Buffer.buffer(buildRequest(query, null, operationName).toJson()));
    }

    @Override
    public Response executeSync(String query, Map<String, Object> variables, String operationName)
            throws ExecutionException, InterruptedException {
        return executeSync(Buffer.buffer(buildRequest(query, variables, operationName).toJson()));
    }

    private Response executeSync(Buffer buffer) throws ExecutionException, InterruptedException {
        HttpResponse<Buffer> result = webClient.postAbs(url)
                .putHeaders(headers)
                .sendBuffer(buffer)
                .toCompletionStage()
                .toCompletableFuture()
                .get();
        return ResponseReader.readFrom(result.bodyAsString(), result.headers().entries());
    }

    @Override
    public Uni<Response> executeAsync(Document document) {
        return executeAsync(buildRequest(document, null, null));
    }

    @Override
    public Uni<Response> executeAsync(Document document, Map<String, Object> variables) {
        return executeAsync(buildRequest(document, variables, null));
    }

    @Override
    public Uni<Response> executeAsync(Document document, String operationName) {
        return executeAsync(buildRequest(document, null, operationName));
    }

    @Override
    public Uni<Response> executeAsync(Document document, Map<String, Object> variables, String operationName) {
        return executeAsync(buildRequest(document, variables, operationName));
    }

    @Override
    public Uni<Response> executeAsync(Request request) {
        return executeAsync(Buffer.buffer(request.toJson()));
    }

    @Override
    public Uni<Response> executeAsync(String query) {
        return executeAsync(Buffer.buffer(buildRequest(query, null, null).toJson()));
    }

    @Override
    public Uni<Response> executeAsync(String query, Map<String, Object> variables) {
        return executeAsync(Buffer.buffer(buildRequest(query, variables, null).toJson()));
    }

    @Override
    public Uni<Response> executeAsync(String query, String operationName) {
        return executeAsync(Buffer.buffer(buildRequest(query, null, operationName).toJson()));
    }

    @Override
    public Uni<Response> executeAsync(String query, Map<String, Object> variables, String operationName) {
        return executeAsync(Buffer.buffer(buildRequest(query, variables, operationName).toJson()));
    }

    private Uni<Response> executeAsync(Buffer buffer) {
        return Uni.createFrom().completionStage(
                webClient.postAbs(url)
                        .putHeaders(headers)
                        .sendBuffer(buffer)
                        .toCompletionStage())
                .map(response -> ResponseReader.readFrom(response.bodyAsString(), response.headers().entries()));
    }

    @Override
    public Multi<Response> subscription(Document document) {
        return subscription0(buildRequest(document, null, null).toJson());
    }

    @Override
    public Multi<Response> subscription(Document document, Map<String, Object> variables) {
        return subscription0(buildRequest(document, variables, null).toJson());
    }

    @Override
    public Multi<Response> subscription(Document document, String operationName) {
        return subscription0(buildRequest(document, null, operationName).toJson());
    }

    @Override
    public Multi<Response> subscription(Document document, Map<String, Object> variables, String operationName) {
        return subscription0(buildRequest(document, variables, operationName).toJson());
    }

    @Override
    public Multi<Response> subscription(Request request) {
        return subscription0(request.toJson());
    }

    @Override
    public Multi<Response> subscription(String query) {
        return subscription0(buildRequest(query, null, null).toJson());
    }

    @Override
    public Multi<Response> subscription(String query, Map<String, Object> variables) {
        return subscription0(buildRequest(query, variables, null).toJson());
    }

    @Override
    public Multi<Response> subscription(String query, String operationName) {
        return subscription0(buildRequest(query, null, operationName).toJson());
    }

    @Override
    public Multi<Response> subscription(String query, Map<String, Object> variables, String operationName) {
        return subscription0(buildRequest(query, variables, operationName).toJson());
    }

    private Multi<Response> subscription0(String requestJson) {
        String WSURL = url.replaceFirst("http", "ws");
        return Multi.createFrom()
                .emitter(e -> {
                    httpClient.webSocketAbs(WSURL, headers, WebsocketVersion.V13, new ArrayList<>(), result -> {
                        if (result.succeeded()) {
                            WebSocket socket = result.result();
                            socket.writeTextMessage(requestJson);
                            socket.handler(message -> {
                                e.emit(ResponseReader.readFrom(message.toString(), Collections.emptyList()));
                            });
                            socket.closeHandler((v) -> {
                                e.complete();
                            });
                            e.onTermination(socket::close);
                        } else {
                            e.fail(result.cause());
                        }
                    });
                });
    }

    private Request buildRequest(Document document, Map<String, Object> variables, String operationName) {
        return buildRequest(document.build(), variables, operationName);
    }

    private Request buildRequest(String query, Map<String, Object> variables, String operationName) {
        RequestImpl request = new RequestImpl(query);
        if (variables != null) {
            request.setVariables(variables);
        }
        if (operationName != null && !operationName.isEmpty()) {
            request.setOperationName(operationName);
        }
        return request;
    }

    @Override
    public void close() {
        httpClient.close();
    }

}
