package io.smallrye.graphql.client.vertx.dynamic;

import static java.util.stream.Collectors.toList;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import javax.json.JsonObject;

import org.jboss.logging.Logger;

import io.smallrye.graphql.client.Request;
import io.smallrye.graphql.client.Response;
import io.smallrye.graphql.client.core.Document;
import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClient;
import io.smallrye.graphql.client.impl.RequestImpl;
import io.smallrye.graphql.client.impl.ResponseReader;
import io.smallrye.graphql.client.vertx.websocket.BuiltinWebsocketSubprotocolHandlers;
import io.smallrye.graphql.client.vertx.websocket.WebSocketSubprotocolHandler;
import io.smallrye.graphql.client.websocket.WebsocketSubprotocol;
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

    private static final Logger log = Logger.getLogger(VertxDynamicGraphQLClient.class);

    private final WebClient webClient;
    private final HttpClient httpClient;
    private final String url;
    private final String websocketUrl;
    private final MultiMap headers;
    private final List<WebsocketSubprotocol> subprotocols;
    private final Integer subscriptionInitializationTimeout;

    private Uni<WebSocketSubprotocolHandler> webSocketHandler;

    VertxDynamicGraphQLClient(Vertx vertx, String url, String websocketUrl, MultiMap headers, WebClientOptions options,
            List<WebsocketSubprotocol> subprotocols, Integer subscriptionInitializationTimeout) {
        if (options != null) {
            this.httpClient = vertx.createHttpClient(options);
        } else {
            this.httpClient = vertx.createHttpClient();
        }
        this.webClient = WebClient.wrap(httpClient);
        this.headers = headers;
        this.url = url;
        this.websocketUrl = websocketUrl;
        this.subprotocols = subprotocols;
        this.subscriptionInitializationTimeout = subscriptionInitializationTimeout;
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
        return ResponseReader.readFrom(result.bodyAsString(), convertHeaders(result.headers()));
    }

    // converts the list of HTTP headers from a `MultiMap`
    // (which is returned by vert.x) to a `Map<String, List<String>>`, which we expose further
    private Map<String, List<String>> convertHeaders(MultiMap input) {
        return input.entries().stream()
                .collect(Collectors.groupingBy(Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
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
                .map(response -> ResponseReader.readFrom(response.bodyAsString(),
                        convertHeaders(response.headers())));
    }

    @Override
    public Multi<Response> subscription(Document document) {
        return subscription0(buildRequest(document, null, null).toJsonObject());
    }

    @Override
    public Multi<Response> subscription(Document document, Map<String, Object> variables) {
        return subscription0(buildRequest(document, variables, null).toJsonObject());
    }

    @Override
    public Multi<Response> subscription(Document document, String operationName) {
        return subscription0(buildRequest(document, null, operationName).toJsonObject());
    }

    @Override
    public Multi<Response> subscription(Document document, Map<String, Object> variables, String operationName) {
        return subscription0(buildRequest(document, variables, operationName).toJsonObject());
    }

    @Override
    public Multi<Response> subscription(Request request) {
        return subscription0(request.toJsonObject());
    }

    @Override
    public Multi<Response> subscription(String query) {
        return subscription0(buildRequest(query, null, null).toJsonObject());
    }

    @Override
    public Multi<Response> subscription(String query, Map<String, Object> variables) {
        return subscription0(buildRequest(query, variables, null).toJsonObject());
    }

    @Override
    public Multi<Response> subscription(String query, String operationName) {
        return subscription0(buildRequest(query, null, operationName).toJsonObject());
    }

    @Override
    public Multi<Response> subscription(String query, Map<String, Object> variables, String operationName) {
        return subscription0(buildRequest(query, variables, operationName).toJsonObject());
    }

    private Multi<Response> subscription0(JsonObject requestJson) {
        Multi<String> rawMulti = Multi.createFrom().emitter(rawEmitter -> {
            webSocketHandler().subscribe().with(handler -> handler.executeMulti(requestJson, rawEmitter));
        });
        return rawMulti
                .onItem().transform(data -> ResponseReader.readFrom(data, Collections.emptyMap()));
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

    private Uni<WebSocketSubprotocolHandler> webSocketHandler() {
        if (webSocketHandler == null) {
            webSocketHandler = Uni.createFrom().emitter(handlerEmitter -> {
                List<String> subprotocolIds = subprotocols.stream().map(i -> i.getProtocolId()).collect(toList());
                httpClient.webSocketAbs(websocketUrl, headers, WebsocketVersion.V13, subprotocolIds,
                        result -> {
                            if (result.succeeded()) {
                                WebSocket webSocket = result.result();
                                WebSocketSubprotocolHandler handler = BuiltinWebsocketSubprotocolHandlers
                                        .createHandlerFor(webSocket.subProtocol(), webSocket,
                                                subscriptionInitializationTimeout);
                                handlerEmitter.complete(handler);
                                log.debug("Using websocket subprotocol handler: " + handler);
                            } else {
                                handlerEmitter.fail(result.cause());
                            }
                        });
            });
            return webSocketHandler;
        } else {
            return webSocketHandler;
        }
    }

}
