package io.smallrye.graphql.client.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.smallrye.graphql.client.GraphQLClientException;
import io.smallrye.graphql.client.GraphQLError;
import io.smallrye.graphql.client.Response;
import io.smallrye.graphql.client.impl.typesafe.json.JsonReader;
import io.smallrye.graphql.client.impl.typesafe.reflection.TypeInfo;

public class ResponseImpl implements Response {

    public static final String STATUS_CODE = "<status-code>";
    public static final String STATUS_MESSAGE = "<status-message>";

    private final ObjectNode data;
    private final List<GraphQLError> errors;
    private final ObjectNode extensions;
    private final Map<String, List<String>> metadata;

    public ResponseImpl(ObjectNode data, List<GraphQLError> errors, Map<String, List<String>> headers) {
        this(data, errors, null, headers);
    }

    public ResponseImpl(ObjectNode data,
            List<GraphQLError> errors,
            ObjectNode extensions,
            Map<String, List<String>> headers) {
        this.data = data;
        this.errors = errors;
        this.extensions = extensions;
        this.metadata = Collections.unmodifiableMap(headers != null ? headers : Collections.emptyMap());
    }

    public ResponseImpl(ObjectNode data, List<GraphQLError> errors, Map<String, List<String>> headers,
            Integer statusCode, String statusMessage) {
        this(data, errors, null, headers, statusCode, statusMessage);
    }

    public ResponseImpl(ObjectNode data, List<GraphQLError> errors, ObjectNode extensions,
            Map<String, List<String>> headers,
            Integer statusCode, String statusMessage) {
        this.data = data;
        this.errors = errors;
        this.extensions = extensions;
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

    @Override
    public <T> T getObject(Class<T> dataType, String rootField) {
        if (data == null || data.isNull() || data.isEmpty()) {
            throw SmallRyeGraphQLClientMessages.msg.noDataInResponse();
        }
        JsonNode value = data.get(rootField);
        if (value == null) {
            // field is missing in the response completely
            throw SmallRyeGraphQLClientMessages.msg.fieldNotFoundInResponse(rootField, fieldNames(data));
        }
        if (value.isNull()) {
            // field is present in the response, but is null
            return null;
        }
        if (value.isObject()) {
            return (T) JsonReader.readJson(rootField, TypeInfo.of(dataType), value, null);
        } else if (value.isArray()) {
            throw SmallRyeGraphQLClientMessages.msg.responseContainsArray(rootField);
        } else {
            Object scalarValue = getScalarValue(value);
            if (scalarValue != null) {
                return (T) scalarValue;
            }
            throw SmallRyeGraphQLClientMessages.msg.unexpectedValueInResponse(rootField, value.getNodeType().toString());
        }
    }

    @Override
    public <T> List<T> getList(Class<T> dataType, String rootField) {
        if (data == null || data.isNull() || data.isEmpty()) {
            throw SmallRyeGraphQLClientMessages.msg.noDataInResponse();
        }
        JsonNode item = data.get(rootField);
        if (item == null) {
            throw SmallRyeGraphQLClientMessages.msg.fieldNotFoundInResponse(rootField, fieldNames(data));
        }
        if (item.isNull()) {
            // field is present in the response, but is null
            return null;
        }
        if (item.isObject()) {
            throw SmallRyeGraphQLClientMessages.msg.responseContainsSingleObject(rootField);
        }
        if (item.isArray()) {
            List<T> result = new ArrayList<>();
            ArrayNode jsonArray = (ArrayNode) item;
            TypeInfo type = TypeInfo.of(dataType);
            jsonArray.forEach(o -> {
                if (o.isObject()) {
                    result.add((T) JsonReader.readJson(rootField, type, o, null));
                } else {
                    result.add((T) getScalarValue(o));
                }
            });
            return result;
        }
        throw SmallRyeGraphQLClientMessages.msg.unexpectedValueInResponse(rootField, item.getNodeType().toString());
    }

    private Object getScalarValue(JsonNode value) {
        switch (value.getNodeType()) {
            case NUMBER:
                return value.longValue();
            case STRING:
                return value.asText();
            case BOOLEAN:
                return value.booleanValue();
            default:
                return null;
        }
    }

    /**
     * Helper to collect field names from an ObjectNode into a Set-like structure
     * for error reporting.
     */
    private static java.util.Set<String> fieldNames(ObjectNode node) {
        java.util.Set<String> names = new java.util.LinkedHashSet<>();
        node.fieldNames().forEachRemaining(names::add);
        return names;
    }

    @Override
    public ObjectNode getData() {
        return data;
    }

    @Override
    public List<GraphQLError> getErrors() {
        return errors;
    }

    @Override
    public ObjectNode getExtensions() {
        return extensions;
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

    @Override
    public boolean hasData() {
        return data != null;
    }

    @Override
    public boolean hasError() {
        return errors != null;
    }

    @Override
    public boolean hasExtensions() {
        return extensions != null && !extensions.isEmpty();
    }

    @Override
    public String toString() {
        return "ResponseImpl{" +
                "data=" + data +
                ", errors=" + errors +
                ", extensions=" + extensions +
                '}';
    }

    public Map<String, List<String>> getHeaders() {
        return metadata;
    }

    @Override
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
