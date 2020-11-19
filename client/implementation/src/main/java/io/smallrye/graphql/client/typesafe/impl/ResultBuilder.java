package io.smallrye.graphql.client.typesafe.impl;

import static io.smallrye.graphql.client.typesafe.impl.json.JsonUtils.isListOf;
import static io.smallrye.graphql.client.typesafe.impl.json.JsonUtils.toMap;
import static java.util.stream.Collectors.joining;
import static javax.json.JsonValue.ValueType.NULL;
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

import io.smallrye.graphql.client.typesafe.api.ErrorOr;
import io.smallrye.graphql.client.typesafe.api.GraphQlClientError;
import io.smallrye.graphql.client.typesafe.api.GraphQlClientException;
import io.smallrye.graphql.client.typesafe.api.SourceLocation;
import io.smallrye.graphql.client.typesafe.impl.json.JsonReader;
import io.smallrye.graphql.client.typesafe.impl.json.JsonUtils;
import io.smallrye.graphql.client.typesafe.impl.reflection.MethodInvocation;

public class ResultBuilder {
    private final MethodInvocation method;
    private final Map<String, String> queryCache;
    private final JsonObject response;
    private JsonObject data;

    public ResultBuilder(MethodInvocation method, Map<String, String> queryCache, String responseString) {
        this.method = method;
        this.queryCache = queryCache;
        this.response = Json.createReader(new StringReader(responseString)).readObject();
    }

    public Object read() {
        data = readData();
        readErrors();
        return (data == null) ? null
                : JsonReader.readJson(method.toString(), method.getReturnType(), data.get(method.getName()));
    }

    private JsonObject readData() {
        if (!response.containsKey("data") || response.isNull("data"))
            return null;
        JsonObject data = response.getJsonObject("data");
        if (!data.containsKey(method.getName()))
            throw new GraphQlClientException("no data for '" + method.getName() + "':\n  " + data);
        return data;
    }

    private void readErrors() {
        JsonArray jsonErrors = response.getJsonArray("errors");
        if (jsonErrors == null)
            return;
        JsonArray unapplied = jsonErrors.stream().filter(error -> !apply(error)).collect(toJsonArray());
        if (unapplied.isEmpty())
            return;
        throw new GraphQlClientException("errors from service",
                unapplied.stream().map(this::convert).collect(Collectors.toList()));
    }

    private boolean apply(JsonValue error) {
        List<Object> path = getPath(error);
        if (path == null || data == null)
            return false;
        JsonPointer pointer = toPointer(path);
        JsonArrayBuilder errors = Json.createArrayBuilder();
        try {
            if (pointer.containsValue(data) && isListOf(pointer.getValue(data), ErrorOr.class.getSimpleName()))
                pointer.getValue(data).asJsonArray().forEach(errors::add);
        } catch (JsonException e) {
            if (referencesNull(path, data))
                return false;
            throw e;
        }
        errors.add(ERROR_MARK.apply((JsonObject) error));
        this.data = pointer.replace(data, errors.build());
        return true;
    }

    private boolean referencesNull(List<Object> path, JsonObject data) {
        for (int i = 1; i < path.size(); i++)
            if (toPointer(path.subList(0, i)).getValue(data).getValueType() == NULL)
                return true;
        return false;
    }

    private JsonPointer toPointer(List<Object> path) {
        return Json.createPointer(path.stream().map(Object::toString).collect(joining("/", "/", "")));
    }

    private GraphQlClientError convert(JsonValue jsonValue) {
        JsonObject jsonObject = jsonValue.asJsonObject();
        return new GraphQlClientError() {
            @Override
            public String getMessage() {
                return jsonObject.getString("message");
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
        JsonArray jsonArray = jsonValue.asJsonObject().getJsonArray("path");
        return (jsonArray == null) ? null : jsonArray.stream().map(JsonUtils::toValue).collect(Collectors.toList());
    }

    private static final JsonPatch ERROR_MARK = Json.createPatchBuilder().add("/__typename", ErrorOr.class.getSimpleName()).build();
}
