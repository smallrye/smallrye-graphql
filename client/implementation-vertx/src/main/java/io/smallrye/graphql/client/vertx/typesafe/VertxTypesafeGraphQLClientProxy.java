package io.smallrye.graphql.client.vertx.typesafe;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import org.jboss.logging.Logger;

import io.smallrye.graphql.client.GraphQLClientException;
import io.smallrye.graphql.client.impl.typesafe.HeaderBuilder;
import io.smallrye.graphql.client.impl.typesafe.QueryBuilder;
import io.smallrye.graphql.client.impl.typesafe.ResultBuilder;
import io.smallrye.graphql.client.impl.typesafe.reflection.FieldInfo;
import io.smallrye.graphql.client.impl.typesafe.reflection.MethodInvocation;
import io.smallrye.graphql.client.impl.typesafe.reflection.TypeInfo;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.subscription.MultiEmitter;
import io.vertx.core.AsyncResult;
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

    private final Map<String, String> queryCache = new HashMap<>();

    private final Map<String, String> additionalHeaders;
    private final URI endpoint;
    private final HttpClient httpClient;
    private final WebClient webClient;

    VertxTypesafeGraphQLClientProxy(
            Map<String, String> additionalHeaders,
            URI endpoint,
            HttpClient httpClient,
            WebClient webClient) {
        this.additionalHeaders = additionalHeaders;
        this.endpoint = endpoint;
        this.httpClient = httpClient;
        this.webClient = webClient;
    }

    Object invoke(Class<?> api, MethodInvocation method) {
        if (method.isDeclaredInObject())
            return method.invoke(this);

        MultiMap headers = HeadersMultiMap.headers()
                .addAll(new HeaderBuilder(api, method, additionalHeaders).build());
        String request = request(method);

        if (method.getReturnType().isUni()) {
            return Uni.createFrom()
                    .completionStage(postAsync(request, headers))
                    .map(response -> new ResultBuilder(method, response.bodyAsString()).read());
        } else if (method.getReturnType().isMulti()) {
            String WSURL = endpoint.toString().replaceFirst("http", "ws");
            return Multi.createFrom()
                    .emitter(emitter -> httpClient.webSocketAbs(WSURL, headers, WebsocketVersion.V13, new ArrayList<>(),
                            result -> handleMultiResult(method, request, emitter, result)));
        } else {
            String response = postSync(request, headers);
            log.debugf("response graphql: %s", response);
            return new ResultBuilder(method, response).read();
        }
    }

    private String request(MethodInvocation method) {
        JsonObjectBuilder request = jsonObjectFactory.createObjectBuilder();
        String query = queryCache.computeIfAbsent(method.getKey(), key -> new QueryBuilder(method).build());
        request.add("query", query);
        request.add("variables", variables(method));
        request.add("operationName", method.getName());
        log.debugf("request graphql: %s", query);
        String requestString = request.build().toString();
        log.debugf("full graphql request: %s", requestString);
        return requestString;
    }

    private JsonObjectBuilder variables(MethodInvocation method) {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        method.valueParameters().forEach(parameter -> builder.add(parameter.getRawName(), value(parameter.getValue())));
        return builder;
    }

    // TODO: the logic for serializing objects into JSON should probably be shared with server-side module
    // through a common module. Also this is not vert.x specific, another reason to move it out of this module
    private JsonValue value(Object value) {
        if (value == null)
            return JsonValue.NULL;
        TypeInfo type = TypeInfo.of(value.getClass());
        if (type.isScalar())
            return scalarValue(value);
        if (type.isCollection())
            return arrayValue(value);
        if (type.isMap())
            return mapValue(value);
        return objectValue(value, type.fields());
    }

    private JsonValue scalarValue(Object value) {
        if (value instanceof String)
            return Json.createValue((String) value);
        if (value instanceof Date)
            return Json.createValue(((Date) value).toInstant().toString());
        if (value instanceof Enum)
            return Json.createValue(((Enum<?>) value).name());
        if (value instanceof Boolean)
            return ((Boolean) value) ? JsonValue.TRUE : JsonValue.FALSE;
        if (value instanceof Byte)
            return Json.createValue((Byte) value);
        if (value instanceof Short)
            return Json.createValue((Short) value);
        if (value instanceof Integer)
            return Json.createValue((Integer) value);
        if (value instanceof Long)
            return Json.createValue((Long) value);
        if (value instanceof Double)
            return Json.createValue((Double) value);
        if (value instanceof Float)
            return Json.createValue((Float) value);
        if (value instanceof BigInteger)
            return Json.createValue((BigInteger) value);
        if (value instanceof BigDecimal)
            return Json.createValue((BigDecimal) value);
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
        return value.getClass().isArray() ? Arrays.asList((Object[]) value) : (Collection<?>) value;
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
        return webClient.postAbs(endpoint.toString())
                .putHeaders(headers)
                .sendBuffer(Buffer.buffer(request))
                .toCompletionStage();
    }

    private void handleMultiResult(MethodInvocation method, String request, MultiEmitter<? super Object> emitter,
            AsyncResult<WebSocket> result) {
        if (result.succeeded()) {
            WebSocket socket = result.result();
            socket.writeTextMessage(request);
            socket.handler(message -> {
                if (!emitter.isCancelled()) {
                    try {
                        Object item = new ResultBuilder(method, message.toString()).read();
                        if (item != null) {
                            emitter.emit(item);
                        } else {
                            emitter.complete();
                        }
                    } catch (GraphQLClientException ex) {
                        // this means there were errors returned from the service, and we couldn't build a result object
                        // (there was no `ErrorOr` on the failing field, etc.)
                        // so we propagate this exception to the subscribers. Unfortunately this causes the subscription
                        // to end even though the server might still be sending more events. This can be avoided by using the ErrorOr
                        // wrapper on the client side.
                        if (!emitter.isCancelled()) {
                            emitter.fail(ex);
                        }
                    }
                } else {
                    // We still received some more messages after the Emitter got cancelled. This can happen
                    // if the server is sending events very quickly and one of them contains an error that can't be applied
                    // (and thus fails the client-side Multi with a GraphQLClientException), in which case we close the websocket
                    // immediately, but if the server was fast enough, we might have received more messages before actually closing the websocket.
                    // But because the Multi has already received a failure, we can't propagate this to the client application anymore.
                    // Let's just log it.
                    log.warn(
                            "Received an additional item for a subscription that has already ended with a failure, dropping it.");
                }
            });
            socket.closeHandler((v) -> emitter.complete());
            emitter.onTermination(socket::close);
        } else {
            emitter.fail(result.cause());
        }
    }

    private String postSync(String request, MultiMap headers) {
        Future<HttpResponse<Buffer>> future = webClient.postAbs(endpoint.toString())
                .putHeaders(headers)
                .sendBuffer(Buffer.buffer(request));
        try {
            HttpResponse<Buffer> result = future.toCompletionStage().toCompletableFuture().get();
            if (result.statusCode() != 200) {
                throw new RuntimeException("expected successful status code but got " +
                        result.statusCode() + " " + result.statusMessage() + ":\n" +
                        result.bodyAsString());
            }
            return result.bodyAsString();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Request failed", e);
        }
    }

    void close() {
        try {
            httpClient.close();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        try {
            webClient.close();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
