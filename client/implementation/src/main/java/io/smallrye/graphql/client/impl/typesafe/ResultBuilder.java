package io.smallrye.graphql.client.impl.typesafe;

import static io.smallrye.graphql.client.impl.typesafe.json.JsonUtils.isListOf;
import static java.util.stream.Collectors.joining;
import static javax.json.stream.JsonCollectors.toJsonArray;

import java.util.List;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonPatch;
import javax.json.JsonPointer;
import javax.json.JsonValue;

import io.smallrye.graphql.client.GraphQLClientException;
import io.smallrye.graphql.client.InvalidResponseException;
import io.smallrye.graphql.client.impl.ResponseReader;
import io.smallrye.graphql.client.impl.typesafe.json.JsonReader;
import io.smallrye.graphql.client.impl.typesafe.json.JsonUtils;
import io.smallrye.graphql.client.impl.typesafe.reflection.MethodInvocation;
import io.smallrye.graphql.client.typesafe.api.ErrorOr;

public class ResultBuilder {
    private final MethodInvocation method;
    private final JsonObject response;
    private final String responseString;
    private final Integer statusCode;
    private final String statusMessage;
    private JsonObject data;

    public ResultBuilder(MethodInvocation method, String responseString) {
        this(method, responseString, null, null);
    }

    public ResultBuilder(MethodInvocation method, String responseString, Integer statusCode, String statusMessage) {
        this.method = method;
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
        this.responseString = responseString;
        this.response = ResponseReader.parseGraphQLResponse(responseString);
    }

    public Object read() {
        if (response == null) {
            throw new InvalidResponseException(
                    "Unexpected response. Code=" + statusCode + ", message=\"" + statusMessage + "\", " +
                            "body=\"" + responseString + "\"");
        }
        data = readData();
        readErrors();
        if (data == null)
            return null;
        JsonValue value = method.isSingle() ? data.get(method.getName()) : data;
        return JsonReader.readJson(method.toString(), method.getReturnType(), value, null);
    }

    private JsonObject readData() {
        if (!response.containsKey("data") || response.isNull("data"))
            return null;
        JsonObject data = response.getJsonObject("data");
        if (method.isSingle() && !data.containsKey(method.getName()))
            throw new InvalidResponseException("No data for '" + method.getName() + "'");
        return data;
    }

    private void readErrors() {
        if (!response.containsKey("errors") || response.isNull("errors"))
            return;
        JsonArray jsonErrors = response.getJsonArray("errors");
        if (jsonErrors == null)
            return;
        JsonArray unapplied = jsonErrors.stream().filter(error -> !apply(error)).collect(toJsonArray());
        if (unapplied.isEmpty())
            return;
        throw new GraphQLClientException("errors from service",
                unapplied.stream().map(ResponseReader::readError).collect(Collectors.toList()));
    }

    private boolean apply(JsonValue error) {
        List<Object> path = getPath(error);
        if (data == null || path == null)
            return false;
        JsonPointer pointer = Json.createPointer(path.stream().map(Object::toString).collect(joining("/", "/", "")));
        if (!exists(pointer))
            return false;
        JsonArrayBuilder errors = Json.createArrayBuilder();
        if (pointer.containsValue(data) && isListOf(pointer.getValue(data), ErrorOr.class.getSimpleName()))
            pointer.getValue(data).asJsonArray().forEach(errors::add);
        errors.add(ERROR_MARK.apply((JsonObject) error));
        this.data = pointer.replace(data, errors.build());
        return true;
    }

    private boolean exists(JsonPointer pointer) {
        try {
            pointer.containsValue(data);
            return true;
        } catch (JsonException e) {
            return false;
        }
    }

    private static List<Object> getPath(JsonValue jsonValue) {
        JsonValue value = jsonValue.asJsonObject().get("path");
        JsonArray jsonArray;
        if (value != null && value.getValueType().equals(JsonValue.ValueType.ARRAY)) {
            jsonArray = value.asJsonArray();
        } else {
            jsonArray = null;
        }
        return (jsonArray == null) ? null : jsonArray.stream().map(JsonUtils::toValue).collect(Collectors.toList());
    }

    private static final JsonPatch ERROR_MARK = Json.createPatchBuilder().add("/__typename", ErrorOr.class.getSimpleName())
            .build();
}
