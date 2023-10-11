package io.smallrye.graphql.client.vertx.typesafe;

import static java.util.stream.Collectors.*;
import static java.util.stream.Collectors.toList;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonBuilderFactory;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonValue;

import org.jboss.logging.Logger;

import io.smallrye.graphql.client.InvalidResponseException;
import io.smallrye.graphql.client.impl.discovery.ServiceURLSupplier;
import io.smallrye.graphql.client.impl.discovery.StaticURLSupplier;
import io.smallrye.graphql.client.impl.discovery.StorkServiceURLSupplier;
import io.smallrye.graphql.client.impl.typesafe.HeaderBuilder;
import io.smallrye.graphql.client.impl.typesafe.QueryBuilder;
import io.smallrye.graphql.client.impl.typesafe.ResultBuilder;
import io.smallrye.graphql.client.impl.typesafe.reflection.FieldInfo;
import io.smallrye.graphql.client.impl.typesafe.reflection.MethodInvocation;
import io.smallrye.graphql.client.impl.typesafe.reflection.TypeInfo;
import io.smallrye.graphql.client.vertx.websocket.BuiltinWebsocketSubprotocolHandlers;
import io.smallrye.graphql.client.vertx.websocket.WebSocketSubprotocolHandler;
import io.smallrye.graphql.client.websocket.WebsocketSubprotocol;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.WebSocket;
import io.vertx.core.http.WebsocketVersion;
import io.vertx.core.http.impl.headers.HeadersMultiMap;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;

class VertxTypesafeGraphQLClientProxy {

    private static final Logger log = Logger.getLogger(VertxTypesafeGraphQLClientProxy.class);

    private static final JsonBuilderFactory jsonObjectFactory = Json.createBuilderFactory(null);

    private final ConcurrentMap<String, String> queryCache = new ConcurrentHashMap<>();

    private final Map<String, String> additionalHeaders;
    private final Map<String, Uni<String>> dynamicHeaders;
    private final Map<String, Object> initPayload;
    private final ServiceURLSupplier endpoint;
    private final ServiceURLSupplier websocketUrl;
    private final HttpClient httpClient;
    private final WebClient webClient;
    private final List<WebsocketSubprotocol> subprotocols;
    private final Integer subscriptionInitializationTimeout;
    private final Class<?> api;
    private final boolean executeSingleOperationsOverWebsocket;
    private final boolean allowUnexpectedResponseFields;

    // Do NOT use this field directly, always retrieve by calling `webSocketHandler()`.
    // When a websocket connection is required, then this is populated with a Uni
    // holding the websocket subprotocol handler (the Uni will be completed
    // when the websocket connection and the handler are ready to use).
    // In case that the websocket connection is lost, this AtomicReference will be set to null,
    // so when another operation requiring a websocket is invoked, the reference will be populated again
    // and a new websocket connection attempted.
    private final AtomicReference<Uni<WebSocketSubprotocolHandler>> webSocketHandler = new AtomicReference<>();

    VertxTypesafeGraphQLClientProxy(
            Class<?> api,
            Map<String, String> additionalHeaders,
            Map<String, Uni<String>> dynamicHeaders,
            Map<String, Object> initPayload,
            URI endpoint,
            String websocketUrl,
            boolean executeSingleOperationsOverWebsocket,
            HttpClient httpClient,
            WebClient webClient,
            List<WebsocketSubprotocol> subprotocols,
            Integer subscriptionInitializationTimeout,
            boolean allowUnexpectedResponseFields) {
        this.api = api;
        this.additionalHeaders = additionalHeaders;
        this.dynamicHeaders = dynamicHeaders;
        this.initPayload = initPayload;
        if (endpoint != null) {
            if (endpoint.getScheme().startsWith("stork")) {
                this.endpoint = new StorkServiceURLSupplier(endpoint, false);
            } else {
                this.endpoint = new StaticURLSupplier(endpoint.toString());
            }
        } else {
            this.endpoint = null;
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
        this.httpClient = httpClient;
        this.webClient = webClient;
        this.subprotocols = subprotocols;
        this.subscriptionInitializationTimeout = subscriptionInitializationTimeout;
        this.allowUnexpectedResponseFields = allowUnexpectedResponseFields;
    }

    Object invoke(MethodInvocation method) {
        if (method.isDeclaredInObject()) {
            return method.invoke(this);
        }

        MultiMap headers = HeadersMultiMap.headers()
                .addAll(new HeaderBuilder(api, method, additionalHeaders).build());
        JsonObject request = request(method);

        if (method.getReturnType().isUni()) {
            if (executeSingleOperationsOverWebsocket) {
                return executeSingleResultOperationOverWebsocket(method, request);
            } else {
                return executeSingleResultOperationOverHttpAsync(method, request, headers);
            }
        } else if (method.getReturnType().isMulti()) {
            return executeSubscriptionOverWebsocket(method, request);
        } else {
            if (executeSingleOperationsOverWebsocket) {
                return executeSingleResultOperationOverWebsocket(method, request).await().indefinitely();
            } else {
                return executeSingleResultOperationOverHttpSync(method, request, headers);
            }
        }
    }

    private Object executeSingleResultOperationOverHttpSync(MethodInvocation method, JsonObject request, MultiMap headers) {
        MultiMap allHeaders = new HeadersMultiMap();
        allHeaders.addAll(headers);
        // obtain values of dynamic headers and add them to the request
        for (Map.Entry<String, Uni<String>> dynamicHeaderEntry : dynamicHeaders.entrySet()) {
            allHeaders.add(dynamicHeaderEntry.getKey(), dynamicHeaderEntry.getValue().await().indefinitely());
        }
        HttpResponse<Buffer> response = postSync(request.toString(), allHeaders);
        if (log.isTraceEnabled() && response != null) {
            log.tracef("response graphql: %s", response.bodyAsString());
        }
        return new ResultBuilder(method, response.bodyAsString(),
                response.statusCode(), response.statusMessage(), convertHeaders(allHeaders),
                allowUnexpectedResponseFields).read();
    }

    private Uni<Object> executeSingleResultOperationOverHttpAsync(MethodInvocation method, JsonObject request,
            MultiMap headers) {
        List<Uni<Void>> unis = new ArrayList<>();
        MultiMap allHeaders = new HeadersMultiMap();
        allHeaders.addAll(headers);
        // obtain values of dynamic headers and add them to the request
        for (Map.Entry<String, Uni<String>> stringUniEntry : dynamicHeaders.entrySet()) {
            unis.add(stringUniEntry.getValue().onItem().invoke(headerValue -> {
                allHeaders.add(stringUniEntry.getKey(), headerValue);
            }).replaceWithVoid());
        }
        if (unis.isEmpty()) {
            return Uni.createFrom().completionStage(postAsync(request.toString(), allHeaders))
                    .map(response -> new ResultBuilder(method, response.bodyAsString(),
                            response.statusCode(), response.statusMessage(), convertHeaders(allHeaders),
                            allowUnexpectedResponseFields).read());
        } else {
            // when all dynamic headers have been obtained, proceed with the request
            return Uni.combine().all().unis(unis)
                    .combinedWith(f -> f)
                    .onItem().transformToUni(g -> Uni.createFrom()
                            .completionStage(postAsync(request.toString(), allHeaders))
                            .map(response -> new ResultBuilder(method, response.bodyAsString(),
                                    response.statusCode(), response.statusMessage(), convertHeaders(allHeaders),
                                    allowUnexpectedResponseFields).read()));
        }
    }

    private Map<String, List<String>> convertHeaders(MultiMap input) {
        return input.entries().stream()
                .collect(groupingBy(Map.Entry::getKey,
                        mapping(Map.Entry::getValue, toList())));
    }

    private Uni<Object> executeSingleResultOperationOverWebsocket(MethodInvocation method, JsonObject request) {
        AtomicReference<String> operationId = new AtomicReference<>();
        AtomicReference<WebSocketSubprotocolHandler> handlerRef = new AtomicReference<>();
        Uni<String> rawUni = Uni.createFrom().emitter(rawEmitter -> {
            webSocketHandler().subscribe().with((handler) -> {
                handlerRef.set(handler);
                operationId.set(handler.executeUni(request, rawEmitter));
            });
        });
        return rawUni
                .onCancellation().invoke(() -> {
                    String id = operationId.get();
                    log.trace("Received onCancellation on operation ID " + id);
                    if (id != null) {
                        handlerRef.get().cancelMulti(id);
                    } else {
                        log.trace("Received onCancellation on an operation that does not have an ID yet");
                    }
                })
                .onItem().transform(data -> {
                    Object object = new ResultBuilder(method, data, allowUnexpectedResponseFields).read();
                    if (object != null) {
                        return object;
                    } else {
                        throw new InvalidResponseException(
                                "Couldn't find neither data nor errors in the response: " + data);
                    }
                });

    }

    private Multi<Object> executeSubscriptionOverWebsocket(MethodInvocation method, JsonObject request) {
        AtomicReference<String> operationId = new AtomicReference<>();
        AtomicReference<WebSocketSubprotocolHandler> handlerRef = new AtomicReference<>();
        Multi<String> rawMulti = Multi.createFrom().emitter(rawEmitter -> {
            webSocketHandler().subscribe().with(handler -> {
                handlerRef.set(handler);
                operationId.set(handler.executeMulti(request, rawEmitter));
            });
        });
        return rawMulti
                .onCancellation().invoke(() -> {
                    handlerRef.get().cancelMulti(operationId.get());
                })
                .onItem().transform(data -> {
                    Object object = new ResultBuilder(method, data, allowUnexpectedResponseFields).read();
                    if (object != null) {
                        return object;
                    } else {
                        throw new InvalidResponseException(
                                "Couldn't find neither data nor errors in the response: " + data);
                    }
                });
    }

    private Uni<WebSocketSubprotocolHandler> webSocketHandler() {
        return webSocketHandler.updateAndGet(currentValue -> {
            if (currentValue == null) {
                return Uni.createFrom().<WebSocketSubprotocolHandler> emitter(handlerEmitter -> {
                    List<String> subprotocolIds = subprotocols.stream().map(i -> i.getProtocolId()).collect(toList());
                    MultiMap headers = HeadersMultiMap.headers()
                            .addAll(new HeaderBuilder(api, null, additionalHeaders).build());
                    websocketUrl.get().subscribe().with(wsUrl -> {
                        httpClient.webSocketAbs(wsUrl, headers, WebsocketVersion.V13, subprotocolIds,
                                result -> {
                                    if (result.succeeded()) {
                                        WebSocket webSocket = result.result();
                                        WebSocketSubprotocolHandler handler = BuiltinWebsocketSubprotocolHandlers
                                                .createHandlerFor(webSocket.subProtocol(), webSocket,
                                                        subscriptionInitializationTimeout, initPayload, () -> {
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

    private JsonObject request(MethodInvocation method) {
        JsonObjectBuilder request = jsonObjectFactory.createObjectBuilder();
        String query = queryCache.computeIfAbsent(method.getKey(), key -> new QueryBuilder(method).build());
        request.add("query", query);
        request.add("variables", variables(method));
        request.add("operationName", method.getName());
        JsonObject result = request.build();
        log.tracef("full graphql request: %s", result.toString());
        return result;
    }

    private JsonObjectBuilder variables(MethodInvocation method) {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        method.valueParameters().forEach(parameter -> builder.add(parameter.getRawName(), value(parameter.getValue())));
        return builder;
    }

    // TODO: the logic for serializing objects into JSON should probably be shared with server-side module
    // through a common module. Also this is not vert.x specific, another reason to move it out of this module
    private JsonValue value(Object value) {
        if (value == null) {
            return JsonValue.NULL;
        }
        TypeInfo type = TypeInfo.of(value.getClass());
        if (type.isScalar()) {
            return scalarValue(value);
        }
        if (type.isCollection()) {
            return arrayValue(value);
        }
        if (type.isMap()) {
            return mapValue(value);
        }
        return objectValue(value, type.fields());
    }

    private JsonValue scalarValue(Object value) {
        if (value instanceof String) {
            return Json.createValue((String) value);
        }
        if (value instanceof Date) {
            return Json.createValue(((Date) value).toInstant().toString());
        }
        if (value instanceof Calendar) {
            return Json.createValue(((Calendar) value).toInstant().toString());
        }
        if (value instanceof Enum) {
            return Json.createValue(((Enum<?>) value).name());
        }
        if (value instanceof Boolean) {
            return ((Boolean) value) ? JsonValue.TRUE : JsonValue.FALSE;
        }
        if (value instanceof Byte) {
            return Json.createValue((Byte) value);
        }
        if (value instanceof Short) {
            return Json.createValue((Short) value);
        }
        if (value instanceof Integer) {
            return Json.createValue((Integer) value);
        }
        if (value instanceof Long) {
            return Json.createValue((Long) value);
        }
        if (value instanceof Double) {
            return Json.createValue((Double) value);
        }
        if (value instanceof Float) {
            return Json.createValue((Float) value);
        }
        if (value instanceof BigInteger) {
            return Json.createValue((BigInteger) value);
        }
        if (value instanceof BigDecimal) {
            return Json.createValue((BigDecimal) value);
        }
        if (value instanceof OptionalInt) {
            OptionalInt optionalValue = ((OptionalInt) value);
            return ((optionalValue.isPresent()) ? Json.createValue(optionalValue.getAsInt()) : JsonValue.NULL);
        }
        if (value instanceof OptionalLong) {
            OptionalLong optionalValue = ((OptionalLong) value);
            return ((optionalValue.isPresent()) ? Json.createValue(optionalValue.getAsLong()) : JsonValue.NULL);
        }
        if (value instanceof OptionalDouble) {
            OptionalDouble optionalValue = ((OptionalDouble) value);
            return ((optionalValue.isPresent()) ? Json.createValue(optionalValue.getAsDouble()) : JsonValue.NULL);
        }
        return Json.createValue(value.toString());
    }

    private JsonArray arrayValue(Object value) {
        JsonArrayBuilder array = Json.createArrayBuilder();
        values(value).forEach(item -> array.add(value(item)));
        return array.build();
    }

    private JsonArray mapValue(Object value) {
        Map<?, ?> map = (Map<?, ?>) value;
        JsonArrayBuilder array = Json.createArrayBuilder();
        map.forEach((k, v) -> {
            JsonObjectBuilder entryBuilder = Json.createObjectBuilder();
            entryBuilder.add("key", value(k));
            entryBuilder.add("value", value(v));
            array.add(entryBuilder.build());
        });
        return array.build();
    }

    private Collection<?> values(Object value) {
        return value.getClass().isArray() ? array(value) : (Collection<?>) value;
    }

    private List<Object> array(Object value) {
        if (value.getClass().getComponentType().isPrimitive()) {
            return primitiveArray(value);
        }
        return Arrays.asList((Object[]) value);
    }

    private List<Object> primitiveArray(Object value) {
        int length = Array.getLength(value);
        List<Object> out = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            out.add(Array.get(value, i));
        }
        return out;
    }

    private JsonObject objectValue(Object object, Stream<FieldInfo> fields) {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        fields.forEach(field -> {
            if (field.isIncludeNull() || field.get(object) != null) {
                builder.add(field.getName(), value(field.get(object)));
            }
        });
        return builder.build();
    }

    private CompletionStage<HttpResponse<Buffer>> postAsync(String request, MultiMap headers) {
        return endpoint.get().subscribeAsCompletionStage()
                .thenCompose(url -> webClient.postAbs(url)
                        .putHeaders(headers)
                        .sendBuffer(Buffer.buffer(request))
                        .toCompletionStage());
    }

    private HttpResponse<Buffer> postSync(String request, MultiMap headers) {
        Future<HttpResponse<Buffer>> future = webClient.postAbs(endpoint.get().await().indefinitely())
                .putHeaders(headers)
                .sendBuffer(Buffer.buffer(request));
        try {
            return future.toCompletionStage().toCompletableFuture().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Request failed", e);
        }
    }

    void close() {
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
}
