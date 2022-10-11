package io.smallrye.graphql.client.vertx.dynamic;

import static java.util.stream.Collectors.toList;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import javax.json.JsonObject;

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
    private final Map<String, Object> initPayload;
    private final List<WebsocketSubprotocol> subprotocols;
    private final Integer subscriptionInitializationTimeout;

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
            MultiMap headers, Map<String, Object> initPayload, WebClientOptions options,
            List<WebsocketSubprotocol> subprotocols, Integer subscriptionInitializationTimeout) {
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
    }

    @Override
    public Response executeSync(Document document) throws ExecutionException, InterruptedException {
        return executeSync(buildRequest(document, null, null).toJsonObject());
    }

    @Override
    public Response executeSync(Document document, Map<String, Object> variables)
            throws ExecutionException, InterruptedException {
        return executeSync(buildRequest(document, variables, null).toJsonObject());
    }

    @Override
    public Response executeSync(Document document, String operationName) throws ExecutionException, InterruptedException {
        return executeSync(buildRequest(document, null, operationName).toJsonObject());
    }

    @Override
    public Response executeSync(Document document, Map<String, Object> variables, String operationName)
            throws ExecutionException, InterruptedException {
        return executeSync(buildRequest(document, variables, operationName).toJsonObject());
    }

    @Override
    public Response executeSync(Request request) throws ExecutionException, InterruptedException {
        return executeSync(request.toJsonObject());
    }

    @Override
    public Response executeSync(String query) throws ExecutionException, InterruptedException {
        return executeSync(buildRequest(query, null, null).toJsonObject());
    }

    @Override
    public Response executeSync(String query, Map<String, Object> variables) throws ExecutionException, InterruptedException {
        return executeSync(buildRequest(query, variables, null).toJsonObject());
    }

    @Override
    public Response executeSync(String query, String operationName) throws ExecutionException, InterruptedException {
        return executeSync(buildRequest(query, null, operationName).toJsonObject());
    }

    @Override
    public Response executeSync(String query, Map<String, Object> variables, String operationName)
            throws ExecutionException, InterruptedException {
        return executeSync(buildRequest(query, variables, operationName).toJsonObject());
    }

    private Response executeSync(JsonObject json) {
        if (executeSingleOperationsOverWebsocket) {
            return executeSingleResultOperationOverWebsocket(json).await().indefinitely();
        } else {
            return executeSingleResultOperationOverHttp(json).await().indefinitely();
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
        return executeAsync(request.toJsonObject());
    }

    @Override
    public Uni<Response> executeAsync(String query) {
        return executeAsync(buildRequest(query, null, null).toJsonObject());
    }

    @Override
    public Uni<Response> executeAsync(String query, Map<String, Object> variables) {
        return executeAsync(buildRequest(query, variables, null).toJsonObject());
    }

    @Override
    public Uni<Response> executeAsync(String query, String operationName) {
        return executeAsync(buildRequest(query, null, operationName).toJsonObject());
    }

    @Override
    public Uni<Response> executeAsync(String query, Map<String, Object> variables, String operationName) {
        return executeAsync(buildRequest(query, variables, operationName).toJsonObject());
    }

    private Uni<Response> executeAsync(JsonObject json) {
        if (executeSingleOperationsOverWebsocket) {
            return executeSingleResultOperationOverWebsocket(json);
        } else {
            return executeSingleResultOperationOverHttp(json);
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

    private Uni<Response> executeSingleResultOperationOverHttp(JsonObject json) {
        return Uni.createFrom().completionStage(
                url.get().subscribeAsCompletionStage().thenCompose(instanceUrl -> webClient.postAbs(instanceUrl)
                        .putHeaders(headers)
                        .sendBuffer(Buffer.buffer(json.toString()))
                        .toCompletionStage()))
                .map(response -> ResponseReader.readFrom(response.bodyAsString(),
                        convertHeaders(response.headers()), response.statusCode(), response.statusMessage()));
    }

    private Uni<Response> executeSingleResultOperationOverWebsocket(JsonObject json) {
        AtomicReference<String> operationId = new AtomicReference<>();
        AtomicReference<WebSocketSubprotocolHandler> handlerRef = new AtomicReference<>();
        Uni<String> rawUni = Uni.createFrom().emitter(rawEmitter -> {
            webSocketHandler().subscribe().with(handler -> {
                handlerRef.set(handler);
                operationId.set(handler.executeUni(json, rawEmitter));
            });
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
            });
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
