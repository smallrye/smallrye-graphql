package io.smallrye.graphql.client.vertx.typesafe;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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

import io.smallrye.graphql.client.impl.GraphQLClientConfiguration;
import io.smallrye.graphql.client.impl.typesafe.QueryBuilder;
import io.smallrye.graphql.client.impl.typesafe.ResultBuilder;
import io.smallrye.graphql.client.impl.typesafe.reflection.FieldInfo;
import io.smallrye.graphql.client.impl.typesafe.reflection.MethodInvocation;
import io.smallrye.graphql.client.impl.typesafe.reflection.TypeInfo;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.WebSocket;
import io.vertx.core.http.WebsocketVersion;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;

class VertxTypesafeGraphQLClientProxy {

    private static final Logger log = Logger.getLogger(VertxTypesafeGraphQLClientProxy.class);
    private static final String APPLICATION_JSON_UTF8 = "application/json;charset=utf-8";

    private static final JsonBuilderFactory jsonObjectFactory = Json.createBuilderFactory(null);

    private final Map<String, String> queryCache = new HashMap<>();
    private final GraphQLClientConfiguration configuration;
    private final URI endpoint;
    private final HttpClient httpClient;
    private final WebClient webClient;

    VertxTypesafeGraphQLClientProxy(
            GraphQLClientConfiguration config,
            URI endpoint,
            HttpClient httpClient,
            WebClient webClient) {
        this.configuration = config;
        this.endpoint = endpoint;
        this.httpClient = httpClient;
        this.webClient = webClient;
    }

    Object invoke(Class<?> api, MethodInvocation method) {
        if (method.isDeclaredInObject())
            return method.invoke(this);

        MultiMap headers = new HeaderBuilder(api,
                method,
                configuration != null ? configuration.getHeaders() : Collections.emptyMap())
                        .build();
        headers.set("Accept", APPLICATION_JSON_UTF8);
        String request = request(method);

        if (method.getReturnType().isUni()) {
            return Uni.createFrom()
                    .completionStage(postAsync(request, headers))
                    .map(response -> new ResultBuilder(method, response.bodyAsString()).read());
        } else if (method.getReturnType().isMulti()) {
            String WSURL = endpoint.toString().replaceFirst("http", "ws");
            return Multi.createFrom()
                    .emitter(e -> {
                        httpClient.webSocketAbs(WSURL, headers, WebsocketVersion.V13, new ArrayList<>(), result -> {
                            if (result.succeeded()) {
                                WebSocket socket = result.result();
                                socket.writeTextMessage(request);
                                socket.handler(message -> {
                                    Object item = new ResultBuilder(method, message.toString()).read();
                                    if (item != null) {
                                        e.emit(new ResultBuilder(method, message.toString()).read());
                                    } else {
                                        e.complete();
                                    }
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

    private JsonValue value(Object value) {
        if (value == null)
            return JsonValue.NULL;
        TypeInfo type = TypeInfo.of(value.getClass());
        if (type.isScalar())
            return scalarValue(value);
        if (type.isCollection())
            return arrayValue(value);
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
                .putHeader("Content-Type", APPLICATION_JSON_UTF8)
                .putHeaders(headers)
                .sendBuffer(Buffer.buffer(request))
                .toCompletionStage();
    }

    private String postSync(String request, MultiMap headers) {
        Future<HttpResponse<Buffer>> future = webClient.postAbs(endpoint.toString())
                .putHeader("Content-Type", APPLICATION_JSON_UTF8)
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
