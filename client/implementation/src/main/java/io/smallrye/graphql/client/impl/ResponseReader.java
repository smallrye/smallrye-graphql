package io.smallrye.graphql.client.impl;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonValue;

import org.jboss.logging.Logger;

import io.smallrye.graphql.client.GraphQLError;

public class ResponseReader {
    private static final Logger LOG = Logger.getLogger(ResponseReader.class.getName());

    public static ResponseImpl readFrom(String input, Map<String, List<String>> headers) {
        if (input == null) {
            throw SmallRyeGraphQLClientMessages.msg.nullResponseBody();
        }
        JsonReader jsonReader = Json.createReader(new StringReader(input));
        JsonObject jsonResponse;
        try {
            jsonResponse = jsonReader.readObject();
        } catch (Exception e) {
            throw SmallRyeGraphQLClientMessages.msg.cannotParseResponse(input, e);
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

        return new ResponseImpl(data, errors, headers);
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
