package io.smallrye.graphql.client.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;

import io.smallrye.graphql.client.GraphQLClientException;
import io.smallrye.graphql.client.GraphQLError;
import io.smallrye.graphql.client.Response;
import io.smallrye.graphql.client.impl.typesafe.json.JsonReader;
import io.smallrye.graphql.client.impl.typesafe.reflection.TypeInfo;

public class ResponseImpl implements Response {

    public static final String STATUS_CODE = "<status-code>";
    public static final String STATUS_MESSAGE = "<status-message>";

    private final JsonObject data;
    private final List<GraphQLError> errors;
    private final Map<String, List<String>> metadata;

    public ResponseImpl(JsonObject data, List<GraphQLError> errors, Map<String, List<String>> headers) {
        this.data = data;
        this.errors = errors;
        this.metadata = Collections.unmodifiableMap(headers != null ? headers : Collections.emptyMap());
    }

    public ResponseImpl(JsonObject data, List<GraphQLError> errors, Map<String, List<String>> headers,
            Integer statusCode, String statusMessage) {
        this.data = data;
        this.errors = errors;
        Map<String, List<String>> meta = new HashMap<>();
        if (headers != null) {
            meta.putAll(headers);
        }
        if (statusCode != null) {
            meta.put(STATUS_CODE, List.of(String.valueOf(statusCode)));
        }
        if (statusMessage != null) {
            meta.put(STATUS_MESSAGE, List.of(statusMessage));
        }
        this.metadata = Collections.unmodifiableMap(meta);
    }

    public <T> T getObject(Class<T> dataType, String rootField) {
        if (data == null || data.equals(JsonValue.NULL) || data.keySet().isEmpty()) {
            throw SmallRyeGraphQLClientMessages.msg.noDataInResponse();
        }
        JsonValue value = data.get(rootField);
        if (value == null) {
            // field is missing in the response completely
            throw SmallRyeGraphQLClientMessages.msg.fieldNotFoundInResponse(rootField, data.keySet());
        }
        if (value.getValueType().equals(JsonValue.ValueType.NULL)) {
            // field is present in the response, but is null
            return null;
        }
        if (value.getValueType().equals(JsonValue.ValueType.OBJECT)) {
            return (T) JsonReader.readJson(rootField, TypeInfo.of(dataType), value.asJsonObject(), null);
        } else if (value.getValueType().equals(JsonValue.ValueType.ARRAY)) {
            throw SmallRyeGraphQLClientMessages.msg.responseContainsArray(rootField);
        } else {
            Object scalarValue = getScalarValue(value);
            if (scalarValue != null) {
                return (T) scalarValue;
            }
            throw SmallRyeGraphQLClientMessages.msg.unexpectedValueInResponse(rootField, value.getValueType().toString());
        }
    }

    public <T> List<T> getList(Class<T> dataType, String rootField) {
        if (data == null || data.equals(JsonValue.NULL) || data.keySet().isEmpty()) {
            throw SmallRyeGraphQLClientMessages.msg.noDataInResponse();
        }
        JsonValue item = data.get(rootField);
        if (item == null) {
            throw SmallRyeGraphQLClientMessages.msg.fieldNotFoundInResponse(rootField, data.keySet());
        }
        if (item.getValueType().equals(JsonValue.ValueType.NULL)) {
            // field is present in the response, but is null
            return null;
        }
        if (item instanceof JsonObject) {
            throw SmallRyeGraphQLClientMessages.msg.responseContainsSingleObject(rootField);
        }
        if (item instanceof JsonArray) {
            List<T> result = new ArrayList<T>();
            JsonArray jsonArray = (JsonArray) item;
            TypeInfo type = TypeInfo.of(dataType);
            jsonArray.forEach(o -> {
                if (o.getValueType().equals(JsonValue.ValueType.OBJECT)) {
                    result.add((T) JsonReader.readJson(rootField, type, o, null));
                } else {
                    result.add((T) getScalarValue(o));
                }
            });
            return result;
        }
        throw SmallRyeGraphQLClientMessages.msg.unexpectedValueInResponse(rootField, item.getValueType().toString());
    }

    private Object getScalarValue(JsonValue value) {
        switch (value.getValueType()) {
            case NUMBER:
                return ((JsonNumber) value).longValue();
            case STRING:
                return ((JsonString) value).getString();
            case TRUE:
                return true;
            case FALSE:
                return false;
            default:
                return null;
        }
    }

    public JsonObject getData() {
        return data;
    }

    public List<GraphQLError> getErrors() {
        return errors;
    }

    /**
     * If there are application errors inside this response, this method converts these errors into a `GraphQLClientException`
     * and throws it. If there are no errors, then this method does nothing.
     */
    // This is currently only in the implementation, if this is deemed useful we might move it up to the `Response` interface
    public void throwExceptionIfErrors() {
        if (!errors.isEmpty()) {
            throw new GraphQLClientException("Errors from service", errors);
        }
    }

    public boolean hasData() {
        return data != null;
    }

    public boolean hasError() {
        return errors != null;
    }

    public String toString() {
        return "GraphQLResponse{" + "data=" + data + ", errors=" + errors + '}';
    }

    public Map<String, List<String>> getHeaders() {
        return metadata;
    }

    public Map<String, List<String>> getTransportMeta() {
        return metadata;
    }

    /**
     * Returns the HTTP status code. Returns null if no HTTP status code is known, for example
     * when this response corresponds to an operation executed over a websocket rather than a simple HTTP exchange.
     */
    public Integer getStatusCode() {
        if (metadata.containsKey(STATUS_CODE)) {
            return Integer.valueOf(metadata.get(STATUS_CODE).get(0));
        } else {
            return null;
        }
    }

    /**
     * Returns the HTTP status message. Returns null if no HTTP status message is known, for example
     * when this response corresponds to an operation executed over a websocket rather than a simple HTTP exchange.
     */
    public String getStatusMessage() {
        if (metadata.containsKey(STATUS_MESSAGE)) {
            return metadata.get(STATUS_MESSAGE).get(0);
        } else {
            return null;
        }
    }
}
