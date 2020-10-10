package io.smallrye.graphql.client.typesafe.impl;

import static javax.json.JsonValue.ValueType.ARRAY;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.Response.Status.Family.SUCCESSFUL;

import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReaderFactory;
import javax.json.JsonValue;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.StatusType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.smallrye.graphql.client.typesafe.api.GraphQlClientException;
import io.smallrye.graphql.client.typesafe.impl.json.JsonReader;
import io.smallrye.graphql.client.typesafe.impl.reflection.FieldInfo;
import io.smallrye.graphql.client.typesafe.impl.reflection.MethodInvocation;
import io.smallrye.graphql.client.typesafe.impl.reflection.TypeInfo;

class GraphQlClientProxy {
    private static final Logger log = LoggerFactory.getLogger(GraphQlClientProxy.class);
    private static final MediaType APPLICATION_JSON_UTF8 = APPLICATION_JSON_TYPE.withCharset("utf-8");

    private static final JsonBuilderFactory jsonObjectFactory = Json.createBuilderFactory(null);
    private static final JsonReaderFactory jsonReaderFactory = Json.createReaderFactory(null);

    private final Map<String, String> queryCache = new HashMap<>();
    private final WebTarget target;

    GraphQlClientProxy(WebTarget target) {
        this.target = target;
    }

    Object invoke(Class<?> api, MethodInvocation method) {
        MultivaluedMap<String, Object> headers = new HeaderBuilder(api, method).build();
        String request = request(method);

        log.info("request graphql: {}", request);
        String response = post(request, headers);
        log.info("response graphql: {}", response);

        return fromJson(method, request, response);
    }

    private String request(MethodInvocation method) {
        JsonObjectBuilder request = jsonObjectFactory.createObjectBuilder();
        request.add("query", queryCache.computeIfAbsent(method.getKey(), key -> new QueryBuilder(method).build()));
        request.add("variables", variables(method));
        request.add("operationName", method.getName());
        return request.build().toString();
    }

    private JsonObjectBuilder variables(MethodInvocation method) {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        method.valueParameters().forEach(parameter -> builder.add(parameter.getName(), value(parameter.getValue())));
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
        fields.forEach(field -> builder.add(field.getName(), value(field.get(object))));
        return builder.build();
    }

    private String post(String request, MultivaluedMap<String, Object> headers) {
        Response response = target
                .request(APPLICATION_JSON_UTF8)
                .headers(headers)
                .post(entity(request, APPLICATION_JSON_UTF8));
        StatusType status = response.getStatusInfo();
        if (status.getFamily() != SUCCESSFUL)
            throw new GraphQlClientException("expected successful status code but got " +
                    status.getStatusCode() + " " + status.getReasonPhrase() + ":\n" +
                    response.readEntity(String.class));
        return response.readEntity(String.class);
    }

    private Object fromJson(MethodInvocation method, String request, String response) {
        JsonObject responseJson = readResponse(request, response);
        JsonValue value = getData(method, responseJson);
        return JsonReader.readFrom(method, value);
    }

    private JsonObject readResponse(String request, String response) {
        JsonObject responseJson = jsonReaderFactory.createReader(new StringReader(response)).readObject();
        if (hasErrors(responseJson))
            throw new GraphQlClientException("errors from service: " + responseJson.getJsonArray("errors") + ":\n  " + request);
        return responseJson;
    }

    private boolean hasErrors(JsonObject responseJson) {
        return responseJson.containsKey("errors")
                && responseJson.get("errors").getValueType() == ARRAY
                && !responseJson.getJsonArray("errors").isEmpty();
    }

    private JsonValue getData(MethodInvocation method, JsonObject responseJson) {
        JsonObject data = responseJson.getJsonObject("data");
        if (!data.containsKey(method.getName()))
            throw new GraphQlClientException("no data for '" + method.getName() + "':\n  " + data);
        return data.get(method.getName());
    }
}
