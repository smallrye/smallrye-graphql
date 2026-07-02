package io.smallrye.graphql.client.vertx.typesafe;

import static java.util.stream.Collectors.*;

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
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import org.jboss.logging.Logger;

import io.smallrye.graphql.client.InvalidResponseException;
import io.smallrye.graphql.client.impl.RequestImpl;
import io.smallrye.graphql.client.impl.discovery.ServiceURLSupplier;
import io.smallrye.graphql.client.impl.discovery.StaticURLSupplier;
import io.smallrye.graphql.client.impl.discovery.StorkServiceURLSupplier;
import io.smallrye.graphql.client.impl.typesafe.HeaderBuilder;
import io.smallrye.graphql.client.impl.typesafe.QueryBuilder;
import io.smallrye.graphql.client.impl.typesafe.ResultBuilder;
import io.smallrye.graphql.client.impl.typesafe.reflection.FieldInfo;
import io.smallrye.graphql.client.impl.typesafe.reflection.MethodInvocation;
import io.smallrye.graphql.client.impl.typesafe.reflection.TypeInfo;
import io.smallrye.graphql.client.model.ClientModel;
import io.smallrye.graphql.client.vertx.websocket.BuiltinWebsocketSubprotocolHandlers;
import io.smallrye.graphql.client.vertx.websocket.WebSocketSubprotocolHandler;
import io.smallrye.graphql.client.websocket.WebsocketSubprotocol;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.WebSocketClient;
import io.vertx.core.http.WebSocketConnectOptions;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

class VertxTypesafeGraphQLClientProxy {

    private static final Logger log = Logger.getLogger(VertxTypesafeGraphQLClientProxy.class);

    private static final ObjectMapper MAPPER = RequestImpl.MAPPER;
    private static final JsonNodeFactory NODES = JsonNodeFactory.instance;

    private final ConcurrentMap<String, String> queryCache = new ConcurrentHashMap<>();

    private final Map<String, String> additionalHeaders;
    private final Map<String, Uni<String>> dynamicHeaders;
    private final Map<String, Object> initPayload;
    private final ServiceURLSupplier endpoint;
    private final ServiceURLSupplier websocketUrl;
    private final HttpClient httpClient;
    private final WebClient webClient;
    private final WebSocketClient webSocketClient;
    private final List<WebsocketSubprotocol> subprotocols;
    private final Integer subscriptionInitializationTimeout;
    private final Class<?> api;
    private final ClientModel clientModel;
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
            ClientModel clientModel,
            Map<String, String> additionalHeaders,
            Map<String, Uni<String>> dynamicHeaders,
            Map<String, Object> initPayload,
            URI endpoint,
            String websocketUrl,
            boolean executeSingleOperationsOverWebsocket,
            HttpClient httpClient,
            WebClient webClient,
            WebSocketClient webSocketClient,
            List<WebsocketSubprotocol> subprotocols,
            Integer subscriptionInitializationTimeout,
            boolean allowUnexpectedResponseFields) {
        this.api = api;
        this.clientModel = clientModel;
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
        this.webSocketClient = webSocketClient;
        this.subprotocols = subprotocols;
        this.subscriptionInitializationTimeout = subscriptionInitializationTimeout;
        this.allowUnexpectedResponseFields = allowUnexpectedResponseFields;
    }

    Object invoke(MethodInvocation method) {
        if (method.isDeclaredInObject()) {
            return method.invoke(this);
        }

        MultiMap headers = MultiMap.caseInsensitiveMultiMap()
                .addAll(new HeaderBuilder(api, method, additionalHeaders).build());
        ObjectNode request = request(method);

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

    private Object executeSingleResultOperationOverHttpSync(MethodInvocation method, ObjectNode request, MultiMap headers) {
        MultiMap allHeaders = MultiMap.caseInsensitiveMultiMap();
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

    private Uni<Object> executeSingleResultOperationOverHttpAsync(MethodInvocation method, ObjectNode request,
            MultiMap headers) {
        return Uni.createFrom().deferred(() -> {
            // Fresh headers per (re)subscription
            MultiMap attemptHeaders = MultiMap.caseInsensitiveMultiMap();
            attemptHeaders.addAll(headers);

            // Gather dynamic headers, preserving multi-values
            List<Uni<Void>> headerUnis = new ArrayList<>();
            for (Map.Entry<String, Uni<String>> e : dynamicHeaders.entrySet()) {
                headerUnis.add(
                        e.getValue()
                                .onItem().invoke(v -> attemptHeaders.add(e.getKey(), v))
                                .replaceWithVoid());
            }

            Uni<Void> ready = headerUnis.isEmpty()
                    ? Uni.createFrom().voidItem()
                    : Uni.combine().all().unis(headerUnis).discardItems();

            return ready
                    .chain(() -> postAsync(request.toString(), attemptHeaders))
                    .invoke(response -> {
                        if (log.isTraceEnabled() && response != null) {
                            log.tracef("response graphql: %s", response.bodyAsString());
                        }
                    })
                    .map(response -> new ResultBuilder(
                            method,
                            response.bodyAsString(),
                            response.statusCode(),
                            response.statusMessage(),
                            convertHeaders(attemptHeaders),
                            allowUnexpectedResponseFields).read());
        });
    }

    private Map<String, List<String>> convertHeaders(MultiMap input) {
        return input.entries().stream()
                .collect(groupingBy(Map.Entry::getKey,
                        mapping(Map.Entry::getValue, toList())));
    }

    private Uni<Object> executeSingleResultOperationOverWebsocket(MethodInvocation method, ObjectNode request) {
        AtomicReference<String> operationId = new AtomicReference<>();
        AtomicReference<WebSocketSubprotocolHandler> handlerRef = new AtomicReference<>();
        Uni<String> rawUni = Uni.createFrom().emitter(rawEmitter -> {
            webSocketHandler().subscribe().with((handler) -> {
                handlerRef.set(handler);
                operationId.set(handler.executeUni(request, rawEmitter));
            }, rawEmitter::fail);
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

    private Multi<Object> executeSubscriptionOverWebsocket(MethodInvocation method, ObjectNode request) {
        AtomicReference<String> operationId = new AtomicReference<>();
        AtomicReference<WebSocketSubprotocolHandler> handlerRef = new AtomicReference<>();
        Multi<String> rawMulti = Multi.createFrom().emitter(rawEmitter -> {
            webSocketHandler().subscribe().with(handler -> {
                handlerRef.set(handler);
                operationId.set(handler.executeMulti(request, rawEmitter));
            }, rawEmitter::fail);
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
                    MultiMap headers = MultiMap.caseInsensitiveMultiMap()
                            .addAll(new HeaderBuilder(api, null, additionalHeaders).build());
                    websocketUrl.get().subscribe().with(wsUrl -> {
                        webSocketClient.connect(new WebSocketConnectOptions()
                                .setAbsoluteURI(wsUrl)
                                .setHeaders(headers)
                                .setSubProtocols(subprotocolIds))
                                .onSuccess(webSocket -> {
                                    WebSocketSubprotocolHandler handler = BuiltinWebsocketSubprotocolHandlers
                                            .createHandlerFor(webSocket.subProtocol(), webSocket,
                                                    subscriptionInitializationTimeout, initPayload, () -> {
                                                        webSocketHandler.set(null);
                                                    });
                                    handlerEmitter.complete(handler);
                                    log.debug("Using websocket subprotocol handler: " + handler);
                                })
                                .onFailure(err -> {
                                    webSocketHandler.set(null);
                                    handlerEmitter.fail(err);
                                });
                    });
                }).memoize().indefinitely();
            } else {
                return currentValue;
            }
        });
    }

    private ObjectNode request(MethodInvocation method) {
        ObjectNode request = MAPPER.createObjectNode();
        String query;
        if (clientModel == null) {
            query = queryCache.computeIfAbsent(method.getKey(), key -> new QueryBuilder(method).build());
        } else {
            query = clientModel.getOperationMap().get(method.getMethodKey());
        }
        request.put("query", query);
        request.set("variables", variables(method));
        request.put("operationName", method.getOperationName());
        log.tracef("full graphql request: %s", request.toString());
        return request;
    }

    private ObjectNode variables(MethodInvocation method) {
        ObjectNode builder = MAPPER.createObjectNode();
        method.valueParameters().forEach(parameter -> builder.set(parameter.getRawName(), value(parameter.getValue())));
        return builder;
    }

    // TODO: the logic for serializing objects into JSON should probably be shared with server-side module
    // through a common module. Also this is not vert.x specific, another reason to move it out of this module
    private JsonNode value(Object value) {
        if (value == null) {
            return NODES.nullNode();
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
        if (type.isOptional()) {
            return optionalValue(value);
        }
        return objectValue(value, type.fields());
    }

    private JsonNode scalarValue(Object value) {
        if (value instanceof String) {
            return NODES.textNode((String) value);
        }
        if (value instanceof java.sql.Date) {
            return NODES.textNode(value.toString());
        }
        if (value instanceof Date) {
            return NODES.textNode(((Date) value).toInstant().toString());
        }
        if (value instanceof Calendar) {
            return NODES.textNode(((Calendar) value).toInstant().toString());
        }
        if (value instanceof Enum) {
            return NODES.textNode(((Enum<?>) value).name());
        }
        if (value instanceof Boolean) {
            return NODES.booleanNode((Boolean) value);
        }
        if (value instanceof Byte) {
            return NODES.numberNode((Byte) value);
        }
        if (value instanceof Short) {
            return NODES.numberNode((Short) value);
        }
        if (value instanceof Integer) {
            return NODES.numberNode((Integer) value);
        }
        if (value instanceof Long) {
            return NODES.numberNode((Long) value);
        }
        if (value instanceof Double) {
            return NODES.numberNode((Double) value);
        }
        if (value instanceof Float) {
            return NODES.numberNode((Float) value);
        }
        if (value instanceof BigInteger) {
            return NODES.numberNode((BigInteger) value);
        }
        if (value instanceof BigDecimal) {
            return NODES.numberNode((BigDecimal) value);
        }
        if (value instanceof OptionalInt) {
            OptionalInt optionalValue = ((OptionalInt) value);
            return optionalValue.isPresent() ? NODES.numberNode(optionalValue.getAsInt()) : NODES.nullNode();
        }
        if (value instanceof OptionalLong) {
            OptionalLong optionalValue = ((OptionalLong) value);
            return optionalValue.isPresent() ? NODES.numberNode(optionalValue.getAsLong()) : NODES.nullNode();
        }
        if (value instanceof OptionalDouble) {
            OptionalDouble optionalValue = ((OptionalDouble) value);
            return optionalValue.isPresent() ? NODES.numberNode(optionalValue.getAsDouble()) : NODES.nullNode();
        }
        return NODES.textNode(value.toString());
    }

    private ArrayNode arrayValue(Object value) {
        ArrayNode array = MAPPER.createArrayNode();
        values(value).forEach(item -> array.add(value(item)));
        return array;
    }

    private ArrayNode mapValue(Object value) {
        Map<?, ?> map = (Map<?, ?>) value;
        ArrayNode array = MAPPER.createArrayNode();
        map.forEach((k, v) -> {
            ObjectNode entryNode = MAPPER.createObjectNode();
            entryNode.set("key", value(k));
            entryNode.set("value", value(v));
            array.add(entryNode);
        });
        return array;
    }

    private JsonNode optionalValue(Object value) {
        Optional<?> optional = (Optional<?>) value;
        return value(optional.orElse(null));
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

    private ObjectNode objectValue(Object object, Stream<FieldInfo> fields) {
        ObjectNode builder = MAPPER.createObjectNode();
        fields.forEach(field -> {
            if (field.isIncludeNull() || field.get(object) != null) {
                builder.set(field.getName(), value(field.get(object)));
            }
        });
        return builder;
    }

    private Uni<HttpResponse<Buffer>> postAsync(String request, MultiMap headers) {
        return endpoint.get()
                .chain(url -> Uni.createFrom().completionStage(
                        webClient.postAbs(url)
                                .putHeaders(headers)
                                .sendBuffer(Buffer.buffer(request))::toCompletionStage));
    }

    private HttpResponse<Buffer> postSync(String request, MultiMap headers) {
        return postAsync(request, headers).await().indefinitely();
    }

    void close() {
        try {
            Uni.createFrom().completionStage(
                    Future.join(httpClient.close(), webSocketClient.close())::toCompletionStage)
                    .await().indefinitely();
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
