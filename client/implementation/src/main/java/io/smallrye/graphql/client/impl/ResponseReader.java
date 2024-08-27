package io.smallrye.graphql.client.impl;

import static io.smallrye.graphql.client.impl.JsonProviderHolder.JSON_PROVIDER;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.json.JsonArray;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonReaderFactory;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;

import org.jboss.logging.Logger;

import io.smallrye.graphql.client.GraphQLError;
import io.smallrye.graphql.client.InvalidResponseException;

public class ResponseReader {
    private static final Logger LOG = Logger.getLogger(ResponseReader.class.getName());
    private static final JsonReaderFactory jsonReaderFactory = JSON_PROVIDER.createReaderFactory(null);

    /**
     * Parse a GraphQL response from the input string.
     * Returns the JSON representation of the response if it is present, `null` if there is none.
     * A GraphQL response is defined as a well-formed JSON document that, on the top level, contains
     * at least one of the keys 'data', 'extensions' and 'errors', but NO OTHER key.
     * (see https://spec.graphql.org/draft/#sec-Response-Format)
     */
    public static JsonObject parseGraphQLResponse(String input) {
        return parseGraphQLResponse(input, false);
    }

    public static JsonObject parseGraphQLResponse(String input, Boolean allowUnexpectedResponseFields) {
        if (input == null) {
            return null;
        }
        try (JsonReader jsonReader = jsonReaderFactory.createReader(new StringReader(input))) {
            JsonObject jsonResponse;
            try {
                jsonResponse = jsonReader.readObject();
            } catch (Exception e) {
                return null;
            }

            // validate that this is what we consider a GraphQL response - else return null
            if (jsonResponse.size() >= 1) {
                return checkExpectedResponseFields(jsonResponse,
                        allowUnexpectedResponseFields);
            } else {
                return null;
            }
        }
    }

    private static JsonObject checkExpectedResponseFields(JsonObject jsonResponse,
            Boolean allowUnexpectedResponseFields) {
        for (String key : jsonResponse.keySet()) {
            if (!key.equalsIgnoreCase("data")
                    && !key.equalsIgnoreCase("errors")
                    && !key.equalsIgnoreCase("extensions")) {
                if (!allowUnexpectedResponseFields)
                    return null;
                LOG.info("Ignored field: " + "'" + key + "' in the response.");
            }
        }
        return jsonResponse;
    }

    public static ResponseImpl readFrom(String input, Map<String, List<String>> headers, Integer statusCode,
            String statusMessage, Boolean allowUnexpectedResponseFields) {
        JsonObject jsonResponse = parseGraphQLResponse(input, allowUnexpectedResponseFields);
        if (jsonResponse == null) {
            throw new InvalidResponseException(
                    "Unexpected response. Code=" + statusCode + ", message=\"" + statusMessage + "\", " +
                            "body=\"" + input + "\"");
        }
        JsonObject data = null;
        if (jsonResponse.containsKey("data")) {
            if (!jsonResponse.isNull("data")) {
                data = jsonResponse.getJsonObject("data");
            } else {
                SmallRyeGraphQLClientLogging.log.noDataInResponse();
            }
        }

        List<GraphQLError> errors = null;
        if (jsonResponse.containsKey("errors")) {
            errors = new ArrayList<>();
            for (JsonValue error : jsonResponse.getJsonArray("errors")) {
                errors.add(readError(error));
            }
        }

        boolean containsKeyExtension = jsonResponse.containsKey("extensions");

        if (containsKeyExtension && !jsonResponse.isNull("extensions")
                && !jsonResponse.get("extensions").getValueType().equals(JsonValue.ValueType.OBJECT)) {
            LOG.warn("Unexpected value of 'extensions' in response: " + jsonResponse.get("extensions"));
        }

        JsonObject extensions = null;
        if (containsKeyExtension && jsonResponse.get("extensions").getValueType().equals(JsonValue.ValueType.OBJECT)) {
            extensions = jsonResponse.getJsonObject("extensions");
        }

        return new ResponseImpl(data, errors, extensions, headers, statusCode, statusMessage);
    }

    public static ResponseImpl readFrom(String input, Map<String, List<String>> headers) {
        return readFrom(input, headers, null, null);
    }

    public static ResponseImpl readFrom(String input, Map<String, List<String>> headers, Integer statusCode,
            String statusMessage) {
        return readFrom(input, headers, statusCode, statusMessage, false);
    }

    public static GraphQLError readError(JsonValue errorJson) {
        JsonObject errorObject = errorJson.asJsonObject();
        GraphQLErrorImpl decodedError = new GraphQLErrorImpl();

        try {
            JsonValue message = errorObject.get("message");
            if (message instanceof JsonString) {
                decodedError.setMessage(errorObject.getString("message"));
            }
        } catch (RuntimeException e) {
            LOG.warn(e);
        }

        try {
            if (errorObject.containsKey("locations")
                    && errorObject.get("locations").getValueType().equals(JsonValue.ValueType.ARRAY)) {
                JsonArray locations = errorObject.getJsonArray("locations");
                List<Map<String, Integer>> locationList = new ArrayList<>();
                for (JsonValue jsonValue : locations) {
                    JsonObject location = jsonValue.asJsonObject();
                    Map<String, Integer> map = new HashMap<>();
                    location.forEach((key, value) -> {
                        // TODO: how to handle non-numeric location segments?
                        if (value instanceof JsonNumber) {
                            map.put(key, ((JsonNumber) value).intValue());
                        }
                    });
                    locationList.add(map);
                }
                decodedError.setLocations(locationList);
            }
        } catch (RuntimeException e) {
            LOG.warn(e);
        }

        try {
            if (errorObject.containsKey("path")
                    && errorObject.get("path").getValueType().equals(JsonValue.ValueType.ARRAY)) {
                Object[] path = new Object[errorObject.getJsonArray("path").size()];
                int i = 0;
                for (JsonValue segment : errorObject.getJsonArray("path")) {
                    switch (segment.getValueType()) {
                        case STRING:
                            path[i] = ((JsonString) segment).getString();
                            break;
                        case NUMBER:
                            path[i] = ((JsonNumber) segment).intValue();
                            break;
                        default:
                            SmallRyeGraphQLClientLogging.log.unknownPathSegmentType(segment.getValueType());
                    }
                    i++;
                }
                decodedError.setPath(path);
            }
        } catch (RuntimeException e) {
            LOG.warn(e);
        }

        try {
            if (errorObject.containsKey("extensions")
                    && errorObject.get("extensions").getValueType().equals(JsonValue.ValueType.OBJECT)) {
                JsonObject extensions = errorObject.getJsonObject("extensions");
                Map<String, Object> extensionMap = new HashMap<>();
                extensions.forEach((key, value) -> {
                    extensionMap.put(key, decode(value));
                });
                decodedError.setExtensions(extensionMap);
            } else {
                decodedError.setExtensions(Collections.emptyMap());
            }
        } catch (RuntimeException e) {
            LOG.warn(e);
        }

        try {
            // check if there are any other fields beyond the ones described by the specification
            Map<String, Object> otherFields = new HashMap<>();
            for (String key : errorObject.keySet()) {
                if (!key.equals("extensions") &&
                        !key.equals("locations") &&
                        !key.equals("message") &&
                        !key.equals("path")) {
                    otherFields.put(key, decode(errorObject.get(key)));
                }
            }
            if (!otherFields.isEmpty()) {
                decodedError.setOtherFields(otherFields);
            }
        } catch (RuntimeException e) {
            LOG.warn(e);
        }

        return decodedError;
    }

    private static Object decode(JsonValue value) {
        switch (value.getValueType()) {
            case STRING:
                return ((JsonString) value).getString();
            case NUMBER:
                return ((JsonNumber) value).longValue();
            case FALSE:
                return false;
            case TRUE:
                return true;
            case NULL:
                return null;
            default:
                return value;
        }
    }
}
