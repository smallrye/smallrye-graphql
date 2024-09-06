package io.smallrye.graphql.client.impl.typesafe;

import static io.smallrye.graphql.client.impl.JsonProviderHolder.JSON_PROVIDER;
import static io.smallrye.graphql.client.impl.typesafe.json.JsonUtils.isListOf;
import static jakarta.json.stream.JsonCollectors.toJsonArray;
import static java.util.stream.Collectors.joining;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonBuilderFactory;
import jakarta.json.JsonException;
import jakarta.json.JsonObject;
import jakarta.json.JsonPatch;
import jakarta.json.JsonPointer;
import jakarta.json.JsonValue;

import io.smallrye.graphql.client.GraphQLClientException;
import io.smallrye.graphql.client.InvalidResponseException;
import io.smallrye.graphql.client.impl.ResponseReader;
import io.smallrye.graphql.client.impl.typesafe.json.JsonReader;
import io.smallrye.graphql.client.impl.typesafe.json.JsonUtils;
import io.smallrye.graphql.client.impl.typesafe.reflection.MethodInvocation;
import io.smallrye.graphql.client.typesafe.api.ErrorOr;
import io.smallrye.graphql.client.typesafe.api.TypesafeResponse;

public class ResultBuilder {
    private static final JsonBuilderFactory jsonBuilderFactory = JSON_PROVIDER.createBuilderFactory(null);

    private final MethodInvocation method;
    private final JsonObject response;
    private final String responseString;
    private final Integer statusCode;
    private final String statusMessage;
    private JsonObject data;
    private JsonObject extensions;
    private Map<String, List<String>> transportMeta;

    public ResultBuilder(MethodInvocation method, String responseString, boolean allowUnexpectedResponseFields) {
        this(method, responseString, null, null, null, allowUnexpectedResponseFields);
    }

    public ResultBuilder(MethodInvocation method,
            String responseString,
            Integer statusCode,
            String statusMessage,
            Map<String, List<String>> transportMeta,
            boolean allowUnexpectedResponseFields) {
        this.method = method;
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
        this.responseString = responseString;
        this.transportMeta = transportMeta;
        this.response = ResponseReader.parseGraphQLResponse(responseString, allowUnexpectedResponseFields);
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
        Object result;
        if (method.getReturnType().isTypesafeResponse()) {
            extensions = readExtensions();
            result = JsonReader.readJsonTypesafeResponse(
                    method.toString(),
                    method.getReturnType(),
                    value,
                    null);
            result = TypesafeResponse.withTransportMetaAndExtensions((TypesafeResponse<?>) result,
                    transportMeta,
                    extensions);
        } else {
            result = JsonReader.readJson(
                    method.toString(),
                    method.getReturnType(),
                    value,
                    null);
        }
        return result;
    }

    private JsonObject readData() {
        if (!response.containsKey("data") || response.isNull("data"))
            return null;

        JsonObject data = response.getJsonObject("data");
        for (String namespace : method.getNamespaces()) {
            data = data.getJsonObject(namespace);
        }

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
        JsonPointer pointer = JSON_PROVIDER.createPointer(path.stream().map(Object::toString).collect(joining("/", "/", "")));
        if (!exists(pointer))
            return false;
        JsonArrayBuilder errors = jsonBuilderFactory.createArrayBuilder();
        if (pointer.containsValue(data) && isListOf(pointer.getValue(data), ErrorOr.class.getSimpleName()))
            pointer.getValue(data).asJsonArray().forEach(errors::add);
        errors.add(ERROR_MARK.apply((JsonObject) error));
        this.data = pointer.replace(data, errors.build());
        return true;
    }

    private JsonObject readExtensions() {
        if (!response.containsKey("extensions") || response.isNull("extensions"))
            return null;
        return response.getJsonObject("extensions");
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

    private static final JsonPatch ERROR_MARK = JSON_PROVIDER.createPatchBuilder()
            .add("/__typename", ErrorOr.class.getSimpleName())
            .build();
}
