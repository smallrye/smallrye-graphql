package io.smallrye.graphql.client.vertx.dynamic;

import static java.util.stream.Collectors.toList;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import jakarta.json.JsonObject;

import org.jboss.logging.Logger;

import io.smallrye.graphql.client.Request;
import io.smallrye.graphql.client.Response;
import io.smallrye.graphql.client.core.Document;
import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClient;
import io.smallrye.graphql.client.impl.RequestImpl;
import io.smallrye.graphql.client.impl.ResponseReader;
import io.smallrye.graphql.client.impl.discovery.ServiceURLSupplier;
import io.smallrye.graphql.client.impl.discovery.StaticURLSupplier;
import io.smallrye.graphql.client.impl.discovery.StorkServiceURLSupplier;
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
import io.vertx.core.http.impl.headers.HeadersMultiMap;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;

public class VertxDynamicGraphQLClient implements DynamicGraphQLClient {

    private static final Logger log = Logger.getLogger(VertxDynamicGraphQLClient.class);

    private final WebClient webClient;
    private final HttpClient httpClient;

    private final ServiceURLSupplier url;
    private final ServiceURLSupplier websocketUrl;

    private final boolean executeSingleOperationsOverWebsocket;
    private final MultiMap headers;
    private final Map<String, Uni<String>> dynamicHeaders;
    private final Map<String, Object> initPayload;
    private final List<WebsocketSubprotocol> subprotocols;
    private final Integer subscriptionInitializationTimeout;
    private final boolean allowUnexpectedResponseFields;

    // Do NOT use this field directly, always retrieve by calling `webSocketHandler()`.
    // When a websocket connection is required, then this is populated with a Uni
    // holding the websocket subprotocol handler (the Uni will be completed
    // when the websocket connection and the handler are ready to use).
    // In case that the websocket connection is lost, this AtomicReference will be set to null,
    // so when another operation requiring a websocket is invoked, the reference will be populated again
    // and a new websocket connection attempted.
    private final AtomicReference<Uni<WebSocketSubprotocolHandler>> webSocketHandler = new AtomicReference<>();

    VertxDynamicGraphQLClient(Vertx vertx, WebClient webClient,
            String url, String websocketUrl, boolean executeSingleOperationsOverWebsocket,
            MultiMap headers, Map<String, Uni<String>> dynamicHeaders,
            Map<String, Object> initPayload, WebClientOptions options,
            List<WebsocketSubprotocol> subprotocols, Integer subscriptionInitializationTimeout,
            boolean allowUnexpectedResponseFields) {
        if (options != null) {
            this.httpClient = vertx.createHttpClient(options);
        } else {
            this.httpClient = vertx.createHttpClient();
        }
        if (webClient == null) {
            this.webClient = WebClient.wrap(httpClient);
        } else {
            this.webClient = webClient;
        }
        this.headers = headers;
        this.dynamicHeaders = dynamicHeaders;
        this.initPayload = initPayload;
        if (url != null) {
            if (url.startsWith("stork")) {
                this.url = new StorkServiceURLSupplier(URI.create(url), false);
            } else {
                this.url = new StaticURLSupplier(url);
            }
        } else {
            this.url = null;
        }
        if (websocketUrl != null) {
            if (websocketUrl.startsWith("stork")) {
                this.websocketUrl = new StorkServiceURLSupplier(URI.create(websocketUrl), true);
            } else {
                this.websocketUrl = new StaticURLSupplier(websocketUrl);
            }
        } else {
            this.websocketUrl = null;
        }

        this.executeSingleOperationsOverWebsocket = executeSingleOperationsOverWebsocket;
        this.subprotocols = subprotocols;
        this.subscriptionInitializationTimeout = subscriptionInitializationTimeout;
        this.allowUnexpectedResponseFields = allowUnexpectedResponseFields;
    }

    @Override
    public Response executeSync(Document document) throws ExecutionException, InterruptedException {
        return executeSync(buildRequest(document, null, null).toJsonObject(), null);
    }

    public Response executeSync(Document document, MultiMap headers) throws ExecutionException, InterruptedException {
        return executeSync(buildRequest(document, null, null).toJsonObject(), headers);
    }

    @Override
    public Response executeSync(Document document, Map<String, Object> variables)
            throws ExecutionException, InterruptedException {
        return executeSync(buildRequest(document, variables, null).toJsonObject(), null);
    }

    public Response executeSync(Document document, Map<String, Object> variables, MultiMap headers)
            throws ExecutionException, InterruptedException {
        return executeSync(buildRequest(document, variables, null).toJsonObject(), headers);
    }

    @Override
    public Response executeSync(Document document, String operationName) throws ExecutionException, InterruptedException {
        return executeSync(buildRequest(document, null, operationName).toJsonObject(), null);
    }

    public Response executeSync(Document document, String operationName, MultiMap headers)
            throws ExecutionException, InterruptedException {
        return executeSync(buildRequest(document, null, operationName).toJsonObject(), headers);
    }

    @Override
    public Response executeSync(Document document, Map<String, Object> variables, String operationName)
            throws ExecutionException, InterruptedException {
        return executeSync(buildRequest(document, variables, operationName).toJsonObject(), null);
    }

    public Response executeSync(Document document, Map<String, Object> variables, String operationName, MultiMap headers)
            throws ExecutionException, InterruptedException {
        return executeSync(buildRequest(document, variables, operationName).toJsonObject(), headers);
    }

    @Override
    public Response executeSync(Request request) throws ExecutionException, InterruptedException {
        return executeSync(request.toJsonObject(), null);
    }

    public Response executeSync(Request request, MultiMap headers) throws ExecutionException, InterruptedException {
        return executeSync(request.toJsonObject(), headers);
    }

    @Override
    public Response executeSync(String query) throws ExecutionException, InterruptedException {
        return executeSync(buildRequest(query, null, null).toJsonObject(), null);
    }

    public Response executeSync(String query, MultiMap headers) throws ExecutionException, InterruptedException {
        return executeSync(buildRequest(query, null, null).toJsonObject(), headers);
    }

    @Override
    public Response executeSync(String query, Map<String, Object> variables) throws ExecutionException, InterruptedException {
        return executeSync(buildRequest(query, variables, null).toJsonObject(), null);
    }

    public Response executeSync(String query, Map<String, Object> variables, MultiMap headers)
            throws ExecutionException, InterruptedException {
        return executeSync(buildRequest(query, variables, null).toJsonObject(), headers);
    }

    @Override
    public Response executeSync(String query, String operationName) throws ExecutionException, InterruptedException {
        return executeSync(buildRequest(query, null, operationName).toJsonObject(), null);
    }

    public Response executeSync(String query, String operationName, MultiMap headers)
            throws ExecutionException, InterruptedException {
        return executeSync(buildRequest(query, null, operationName).toJsonObject(), headers);
    }

    @Override
    public Response executeSync(String query, Map<String, Object> variables, String operationName)
            throws ExecutionException, InterruptedException {
        return executeSync(buildRequest(query, variables, operationName).toJsonObject(), null);
    }

    public Response executeSync(String query, Map<String, Object> variables, String operationName, MultiMap headers)
            throws ExecutionException, InterruptedException {
        return executeSync(buildRequest(query, variables, operationName).toJsonObject(), headers);
    }

    private Response executeSync(JsonObject json, MultiMap additionalHeaders) {
        if (executeSingleOperationsOverWebsocket) {
            return executeSingleResultOperationOverWebsocket(json).await().indefinitely();
        } else {
            MultiMap allHeaders = new HeadersMultiMap().addAll(this.headers);
            if (additionalHeaders != null) {
                allHeaders.addAll(additionalHeaders);
            }
            // obtain values of dynamic headers and add them to the request
            for (Map.Entry<String, Uni<String>> dynamicHeaderEntry : dynamicHeaders.entrySet()) {
                allHeaders.add(dynamicHeaderEntry.getKey(), dynamicHeaderEntry.getValue().await().indefinitely());
            }
            return executeSingleResultOperationOverHttp(json, allHeaders).await().indefinitely();
        }
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
        return executeAsync(buildRequest(document, null, null).toJsonObject(), null);
    }

    public Uni<Response> executeAsync(Document document, MultiMap headers) {
        return executeAsync(buildRequest(document, null, null).toJsonObject(), headers);
    }

    @Override
    public Uni<Response> executeAsync(Document document, Map<String, Object> variables) {
        return executeAsync(buildRequest(document, variables, null).toJsonObject(), null);
    }

    public Uni<Response> executeAsync(Document document, Map<String, Object> variables, MultiMap headers) {
        return executeAsync(buildRequest(document, variables, null).toJsonObject(), headers);
    }

    @Override
    public Uni<Response> executeAsync(Document document, String operationName) {
        return executeAsync(buildRequest(document, null, operationName).toJsonObject(), null);
    }

    public Uni<Response> executeAsync(Document document, String operationName, MultiMap headers) {
        return executeAsync(buildRequest(document, null, operationName).toJsonObject(), headers);
    }

    @Override
    public Uni<Response> executeAsync(Document document, Map<String, Object> variables, String operationName) {
        return executeAsync(buildRequest(document, variables, operationName));
    }

    public Uni<Response> executeAsync(Document document, Map<String, Object> variables, String operationName,
            MultiMap headers) {
        return executeAsync(buildRequest(document, variables, operationName).toJsonObject(), headers);
    }

    @Override
    public Uni<Response> executeAsync(Request request) {
        return executeAsync(request.toJsonObject(), null);
    }

    public Uni<Response> executeAsync(Request request, MultiMap headers) {
        return executeAsync(request.toJsonObject(), headers);
    }

    @Override
    public Uni<Response> executeAsync(String query) {
        return executeAsync(buildRequest(query, null, null).toJsonObject(), null);
    }

    public Uni<Response> executeAsync(String query, MultiMap headers) {
        return executeAsync(buildRequest(query, null, null).toJsonObject(), headers);
    }

    @Override
    public Uni<Response> executeAsync(String query, Map<String, Object> variables) {
        return executeAsync(buildRequest(query, variables, null).toJsonObject(), null);
    }

    public Uni<Response> executeAsync(String query, Map<String, Object> variables, MultiMap headers) {
        return executeAsync(buildRequest(query, variables, null).toJsonObject(), headers);
    }

    @Override
    public Uni<Response> executeAsync(String query, String operationName) {
        return executeAsync(buildRequest(query, null, operationName).toJsonObject(), null);
    }

    public Uni<Response> executeAsync(String query, String operationName, MultiMap headers) {
        return executeAsync(buildRequest(query, null, operationName).toJsonObject(), headers);
    }

    @Override
    public Uni<Response> executeAsync(String query, Map<String, Object> variables, String operationName) {
        return executeAsync(buildRequest(query, variables, operationName).toJsonObject(), null);
    }

    public Uni<Response> executeAsync(String query, Map<String, Object> variables, String operationName, MultiMap headers) {
        return executeAsync(buildRequest(query, variables, operationName).toJsonObject(), headers);
    }

    private Uni<Response> executeAsync(JsonObject json, MultiMap additionalHeaders) {
        if (executeSingleOperationsOverWebsocket) {
            return executeSingleResultOperationOverWebsocket(json);
        } else {
            MultiMap allHeaders = new HeadersMultiMap().addAll(this.headers);
            if (additionalHeaders != null) {
                allHeaders.addAll(additionalHeaders);
            }
            List<Uni<Void>> unis = new ArrayList<>();
            // append dynamic headers to the request
            for (Map.Entry<String, Uni<String>> stringUniEntry : dynamicHeaders.entrySet()) {
                unis.add(stringUniEntry.getValue().onItem().invoke(headerValue -> {
                    allHeaders.add(stringUniEntry.getKey(), headerValue);
                }).replaceWithVoid());
            }
            if (unis.isEmpty()) {
                return executeSingleResultOperationOverHttp(json, allHeaders);
            } else {
                return Uni.combine().all().unis(unis)
                        .combinedWith(f -> f).onItem()
                        .transformToUni(f -> executeSingleResultOperationOverHttp(json, allHeaders));
            }
        }
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

    private Multi<Response> subscription0(JsonObject json) {
        return executeSubscriptionOverWebsocket(json);
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
        try {
            httpClient.close();
        } catch (Throwable t) {
            log.warn(t);
        }
        try {
            webClient.close();
        } catch (Throwable t) {
            log.warn(t);
        }
    }

    private Uni<WebSocketSubprotocolHandler> webSocketHandler() {
        return webSocketHandler.updateAndGet(currentValue -> {
            if (currentValue == null) {
                // if we don't have a handler, create a new one
                return Uni.createFrom().<WebSocketSubprotocolHandler> emitter(handlerEmitter -> {
                    List<String> subprotocolIds = subprotocols.stream().map(i -> i.getProtocolId()).collect(toList());
                    websocketUrl.get().subscribe().with(websocketUrl -> {
                        httpClient.webSocketAbs(websocketUrl, headers, WebsocketVersion.V13, subprotocolIds,
                                result -> {
                                    if (result.succeeded()) {
                                        WebSocket webSocket = result.result();
                                        WebSocketSubprotocolHandler handler = BuiltinWebsocketSubprotocolHandlers
                                                .createHandlerFor(webSocket.subProtocol(), webSocket,
                                                        subscriptionInitializationTimeout, initPayload, () -> {
                                                            // if the websocket disconnects, remove the handler so we can try
                                                            // connecting again with a new websocket and handler
                                                            webSocketHandler.set(null);
                                                        });
                                        handlerEmitter.complete(handler);
                                        log.debug("Using websocket subprotocol handler: " + handler);
                                    } else {
                                        webSocketHandler.set(null);
                                        handlerEmitter.fail(result.cause());
                                    }
                                });
                    });
                }).memoize().indefinitely();
            } else {
                return currentValue;
            }
        });
    }

    private Uni<Response> executeSingleResultOperationOverHttp(JsonObject json, MultiMap allHeaders) {
        return Uni.createFrom().completionStage(
                url.get().subscribeAsCompletionStage().thenCompose(instanceUrl -> webClient.postAbs(instanceUrl)
                        .putHeaders(allHeaders)
                        .sendBuffer(Buffer.buffer(json.toString()))
                        .toCompletionStage()))
                .map(response -> ResponseReader.readFrom(response.bodyAsString(),
                        convertHeaders(response.headers()), response.statusCode(), response.statusMessage(),
                        allowUnexpectedResponseFields));
    }

    private Uni<Response> executeSingleResultOperationOverWebsocket(JsonObject json) {
        AtomicReference<String> operationId = new AtomicReference<>();
        AtomicReference<WebSocketSubprotocolHandler> handlerRef = new AtomicReference<>();
        Uni<String> rawUni = Uni.createFrom().emitter(rawEmitter -> {
            webSocketHandler().subscribe().with(handler -> {
                handlerRef.set(handler);
                operationId.set(handler.executeUni(json, rawEmitter));
            }, rawEmitter::fail);
        });
        return rawUni
                .onCancellation().invoke(() -> {
                    String id = operationId.get();
                    log.trace("Received onCancellation on operation ID " + id);
                    if (id != null) {
                        handlerRef.get().cancelUni(id);
                    } else {
                        log.trace("Received onCancellation on an operation that does not have an ID yet");
                    }
                })
                .onItem().transform(data -> ResponseReader.readFrom(data, Collections.emptyMap()));
    }

    private Multi<Response> executeSubscriptionOverWebsocket(JsonObject json) {
        AtomicReference<String> operationId = new AtomicReference<>();
        AtomicReference<WebSocketSubprotocolHandler> handlerRef = new AtomicReference<>();
        Multi<String> rawMulti = Multi.createFrom().emitter(rawEmitter -> {
            webSocketHandler().subscribe().with(handler -> {
                handlerRef.set(handler);
                operationId.set(handler.executeMulti(json, rawEmitter));
            }, rawEmitter::fail);
        });
        return rawMulti
                .onCancellation().invoke(() -> {
                    String id = operationId.get();
                    log.trace("Received onCancellation on operation ID " + id);
                    if (id != null) {
                        handlerRef.get().cancelMulti(id);
                    } else {
                        log.trace("Received onCancellation on an operation that does not have an ID yet");
                    }
                })
                .onItem().transform(data -> ResponseReader.readFrom(data, Collections.emptyMap()));
    }

}
