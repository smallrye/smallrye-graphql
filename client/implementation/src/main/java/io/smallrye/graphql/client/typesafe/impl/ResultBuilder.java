package io.smallrye.graphql.client.typesafe.impl;

import static io.smallrye.graphql.client.typesafe.impl.json.JsonUtils.isListOf;
import static io.smallrye.graphql.client.typesafe.impl.json.JsonUtils.toMap;
import static java.util.stream.Collectors.joining;
import static javax.json.stream.JsonCollectors.toJsonArray;

import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonPatch;
import javax.json.JsonPointer;
import javax.json.JsonValue;

import io.smallrye.graphql.client.InvalidResponseException;
import io.smallrye.graphql.client.typesafe.api.ErrorOr;
import io.smallrye.graphql.client.typesafe.api.GraphQLClientError;
import io.smallrye.graphql.client.typesafe.api.GraphQLClientException;
import io.smallrye.graphql.client.typesafe.api.SourceLocation;
import io.smallrye.graphql.client.typesafe.impl.json.JsonReader;
import io.smallrye.graphql.client.typesafe.impl.json.JsonUtils;
import io.smallrye.graphql.client.typesafe.impl.reflection.MethodInvocation;

public class ResultBuilder {
    private final MethodInvocation method;
    private final JsonObject response;
    private JsonObject data;

    public ResultBuilder(MethodInvocation method, String responseString) {
        this.method = method;
        this.response = Json.createReader(new StringReader(responseString)).readObject();
    }

    public Object read() {
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
                unapplied.stream().map(this::convert).collect(Collectors.toList()));
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

    private GraphQLClientError convert(JsonValue jsonValue) {
        JsonObject jsonObject = jsonValue.asJsonObject();
        return new GraphQLClientError() {
            @Override
            public String getMessage() {
                return jsonObject.getString("message", null);
            }

            @Override
            public List<SourceLocation> getLocations() {
                JsonArray locations = jsonObject.getJsonArray("locations");
                return (locations == null) ? null : locations.stream().map(this::toSourceLocation).collect(Collectors.toList());
            }

            private SourceLocation toSourceLocation(JsonValue jsonValue) {
                JsonObject jsonObject = jsonValue.asJsonObject();
                return new SourceLocation(
                        jsonObject.getInt("line", 0),
                        jsonObject.getInt("column", 0),
                        jsonObject.getString("sourceName", null));
            }

            @Override
            public List<Object> getPath() {
                return ResultBuilder.getPath(jsonObject);
            }

            @Override
            public Map<String, Object> getExtensions() {
                return toMap(jsonObject.getJsonObject("extensions"));
            }

            @Override
            public String toString() {
                return defaultToString();
            }
        };
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
