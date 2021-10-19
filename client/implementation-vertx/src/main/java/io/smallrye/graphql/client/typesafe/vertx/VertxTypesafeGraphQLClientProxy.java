package io.smallrye.graphql.client.typesafe.vertx;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
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

import io.smallrye.graphql.client.GraphQLClientConfiguration;
import io.smallrye.graphql.client.typesafe.impl.QueryBuilder;
import io.smallrye.graphql.client.typesafe.impl.ResultBuilder;
import io.smallrye.graphql.client.typesafe.impl.reflection.FieldInfo;
import io.smallrye.graphql.client.typesafe.impl.reflection.MethodInvocation;
import io.smallrye.graphql.client.typesafe.impl.reflection.TypeInfo;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;

class VertxTypesafeGraphQLClientProxy {

    private static final Logger log = Logger.getLogger(VertxTypesafeGraphQLClientProxy.class);
    private static final String APPLICATION_JSON_UTF8 = "application/json;charset=utf-8";

    private static final JsonBuilderFactory jsonObjectFactory = Json.createBuilderFactory(null);

    private final Map<String, String> queryCache = new HashMap<>();
    private final Vertx vertx;
    private final WebClient webClient;
    private final GraphQLClientConfiguration configuration;
    private final URI endpoint;

    VertxTypesafeGraphQLClientProxy(Vertx vertx,
            GraphQLClientConfiguration config,
            WebClientOptions options,
            URI endpoint,
            WebClient webClient) {
        this.vertx = vertx;
        this.configuration = config;
        if (webClient != null) {
            this.webClient = webClient;
        } else {
            HttpClient httpClient = options != null ? vertx.createHttpClient(options) : vertx.createHttpClient();
            this.webClient = WebClient.wrap(httpClient);
        }
        this.endpoint = endpoint;
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

        String response = post(request, headers);
        log.debugf("response graphql: %s", response);

        return new ResultBuilder(method, response).read();
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

    private String post(String request, MultiMap headers) {
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
        webClient.close();
    }
}
