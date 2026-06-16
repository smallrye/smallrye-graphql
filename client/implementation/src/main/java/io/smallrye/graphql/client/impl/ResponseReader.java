package io.smallrye.graphql.client.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.logging.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.smallrye.graphql.client.GraphQLError;
import io.smallrye.graphql.client.InvalidResponseException;

public class ResponseReader {
    private static final Logger LOG = Logger.getLogger(ResponseReader.class.getName());
    private static final ObjectMapper MAPPER = RequestImpl.MAPPER;

    /**
     * Parse a GraphQL response from the input string.
     * Returns the JSON representation of the response if it is present, `null` if there is none.
     * A GraphQL response is defined as a well-formed JSON document that, on the top level, contains
     * at least one of the keys 'data', 'extensions' and 'errors', but NO OTHER key.
     * (see https://spec.graphql.org/draft/#sec-Response-Format)
     */
    public static ObjectNode parseGraphQLResponse(String input) {
        return parseGraphQLResponse(input, false);
    }

    public static ObjectNode parseGraphQLResponse(String input, Boolean allowUnexpectedResponseFields) {
        if (input == null) {
            return null;
        }
        try {
            JsonNode parsed = MAPPER.readTree(input);
            if (!parsed.isObject()) {
                return null;
            }
            ObjectNode jsonResponse = (ObjectNode) parsed;

            // validate that this is what we consider a GraphQL response - else return null
            if (jsonResponse.size() >= 1) {
                return checkExpectedResponseFields(jsonResponse,
                        allowUnexpectedResponseFields);
            } else {
                return null;
            }
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private static ObjectNode checkExpectedResponseFields(ObjectNode jsonResponse,
            Boolean allowUnexpectedResponseFields) {
        var fieldNames = jsonResponse.fieldNames();
        while (fieldNames.hasNext()) {
            String key = fieldNames.next();
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
        ObjectNode jsonResponse = parseGraphQLResponse(input, allowUnexpectedResponseFields);
        if (jsonResponse == null) {
            throw new InvalidResponseException(
                    "Unexpected response. Code=" + statusCode + ", message=\"" + statusMessage + "\", " +
                            "body=\"" + input + "\"",
                    null, headers);
        }
        ObjectNode data = null;
        if (jsonResponse.has("data")) {
            JsonNode dataNode = jsonResponse.get("data");
            if (!dataNode.isNull()) {
                data = (ObjectNode) dataNode;
            } else {
                SmallRyeGraphQLClientLogging.log.noDataInResponse();
            }
        }

        List<GraphQLError> errors = null;
        if (jsonResponse.has("errors")) {
            errors = new ArrayList<>();
            for (JsonNode error : jsonResponse.get("errors")) {
                errors.add(readError(error));
            }
        }

        boolean containsKeyExtension = jsonResponse.has("extensions");

        if (containsKeyExtension && !jsonResponse.get("extensions").isNull()
                && !jsonResponse.get("extensions").isObject()) {
            LOG.warn("Unexpected value of 'extensions' in response: " + jsonResponse.get("extensions"));
        }

        ObjectNode extensions = null;
        if (containsKeyExtension && jsonResponse.get("extensions").isObject()) {
            extensions = (ObjectNode) jsonResponse.get("extensions");
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

    public static GraphQLError readError(JsonNode errorJson) {
        ObjectNode errorObject = (ObjectNode) errorJson;
        GraphQLErrorImpl decodedError = new GraphQLErrorImpl();

        try {
            JsonNode message = errorObject.get("message");
            if (message != null && message.isTextual()) {
                decodedError.setMessage(message.asText());
            }
        } catch (RuntimeException e) {
            LOG.warn(e);
        }

        try {
            if (errorObject.has("locations")
                    && errorObject.get("locations").isArray()) {
                ArrayNode locations = (ArrayNode) errorObject.get("locations");
                List<Map<String, Integer>> locationList = new ArrayList<>();
                for (JsonNode jsonValue : locations) {
                    ObjectNode location = (ObjectNode) jsonValue;
                    Map<String, Integer> map = new HashMap<>();
                    location.fields().forEachRemaining(entry -> {
                        // TODO: how to handle non-numeric location segments?
                        if (entry.getValue().isNumber()) {
                            map.put(entry.getKey(), entry.getValue().intValue());
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
            if (errorObject.has("path")
                    && errorObject.get("path").isArray()) {
                ArrayNode pathArray = (ArrayNode) errorObject.get("path");
                Object[] path = new Object[pathArray.size()];
                int i = 0;
                for (JsonNode segment : pathArray) {
                    switch (segment.getNodeType()) {
                        case STRING:
                            path[i] = segment.asText();
                            break;
                        case NUMBER:
                            path[i] = segment.intValue();
                            break;
                        default:
                            SmallRyeGraphQLClientLogging.log.unknownPathSegmentType(segment.getNodeType());
                    }
                    i++;
                }
                decodedError.setPath(path);
            }
        } catch (RuntimeException e) {
            LOG.warn(e);
        }

        try {
            if (errorObject.has("extensions")
                    && errorObject.get("extensions").isObject()) {
                ObjectNode extensions = (ObjectNode) errorObject.get("extensions");
                Map<String, Object> extensionMap = new HashMap<>();
                extensions.fields().forEachRemaining(entry -> {
                    extensionMap.put(entry.getKey(), decode(entry.getValue()));
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
            errorObject.fieldNames().forEachRemaining(key -> {
                if (!key.equals("extensions") &&
                        !key.equals("locations") &&
                        !key.equals("message") &&
                        !key.equals("path")) {
                    otherFields.put(key, decode(errorObject.get(key)));
                }
            });
            if (!otherFields.isEmpty()) {
                decodedError.setOtherFields(otherFields);
            }
        } catch (RuntimeException e) {
            LOG.warn(e);
        }

        return decodedError;
    }

    private static Object decode(JsonNode value) {
        switch (value.getNodeType()) {
            case STRING:
                return value.asText();
            case NUMBER:
                return value.longValue();
            case BOOLEAN:
                return value.booleanValue();
            case NULL:
                return null;
            default:
                return value;
        }
    }
}
