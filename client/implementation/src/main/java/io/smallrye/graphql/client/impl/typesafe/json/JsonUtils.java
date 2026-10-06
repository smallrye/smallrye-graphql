package io.smallrye.graphql.client.impl.typesafe.json;

import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.json.Json;
import jakarta.json.stream.JsonParser;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

public class JsonUtils {
    public static Object toValue(JsonNode value) {
        switch (value.getNodeType()) {
            case NULL:
                return null;
            case BOOLEAN:
                return value.booleanValue();
            case STRING:
                return value.asText();
            case NUMBER:
                return value.numberValue();
            case ARRAY:
                return toList((ArrayNode) value);
            case OBJECT:
                return toMap((ObjectNode) value);
            default:
                break;
        }
        throw new UnsupportedOperationException();
    }

    public static List<Object> toList(ArrayNode value) {
        List<Object> result = new ArrayList<>();
        for (JsonNode node : value) {
            result.add(toValue(node));
        }
        return result;
    }

    public static Map<String, Object> toMap(ObjectNode jsonObject) {
        if (jsonObject == null)
            return null;
        Map<String, Object> map = new LinkedHashMap<>();
        jsonObject.properties().forEach(entry -> map.put(entry.getKey(), toValue(entry.getValue())));
        return map;
    }

    public static boolean isListOf(JsonNode jsonValue, String typename) {
        return jsonValue.isArray()
                && jsonValue.size() > 0
                && jsonValue.get(0).isObject()
                && typename.equals(
                        jsonValue.get(0).has("__typename") ? jsonValue.get(0).get("__typename").asText(null) : null);
    }

    public static boolean IS_PARSON_PRESENT = checkParsson();

    public static boolean checkParsson() {
        try {
            // is there any JsonParserImpl present?
            try (JsonParser parser = Json.createParser(Reader.nullReader())) {
                return true;
            }
        } catch (jakarta.json.JsonException ex1) {
            return false;
        }
    }
}
