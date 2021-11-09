package io.smallrye.graphql.client.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;

import io.smallrye.graphql.client.GraphQLError;
import io.smallrye.graphql.client.Response;
import io.smallrye.graphql.client.impl.typesafe.json.JsonReader;
import io.smallrye.graphql.client.impl.typesafe.reflection.TypeInfo;

public class ResponseImpl implements Response {

    private final JsonObject data;
    private final List<GraphQLError> errors;
    private final Map<String, List<String>> headers;

    public ResponseImpl(JsonObject data, List<GraphQLError> errors, Map<String, List<String>> headers) {
        this.data = data;
        this.errors = errors;
        this.headers = Collections.unmodifiableMap(headers != null ? headers : Collections.emptyMap());
    }

    public <T> T getObject(Class<T> dataType, String rootField) {
        if (data == null || data.equals(JsonValue.NULL) || data.keySet().isEmpty()) {
            throw SmallRyeGraphQLClientMessages.msg.noDataInResponse();
        }
        JsonValue value = data.get(rootField);
        if (value == null || value.getValueType().equals(JsonValue.ValueType.NULL)) {
            throw SmallRyeGraphQLClientMessages.msg.fieldNotFoundInResponse(rootField, data.keySet());
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
        return headers;
    }

    public Map<String, List<String>> getTransportMeta() {
        return headers;
    }
}
