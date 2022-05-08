package io.smallrye.graphql.client.impl.typesafe.json;

import static jakarta.json.JsonValue.ValueType.ARRAY;
import static jakarta.json.JsonValue.ValueType.OBJECT;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import jakarta.json.JsonArray;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;

public class JsonUtils {
    public static Object toValue(JsonValue value) {
        switch (value.getValueType()) {
            case NULL:
                return null;
            case TRUE:
                return true;
            case FALSE:
                return false;
            case STRING:
                return ((JsonString) value).getString();
            case NUMBER:
                return ((JsonNumber) value).numberValue();
            case ARRAY:
                return toList(value.asJsonArray());
            case OBJECT:
                return toMap(value.asJsonObject());
        }
        throw new UnsupportedOperationException();
    }

    public static Object toList(JsonArray value) {
        return value.stream().map(JsonUtils::toValue).collect(Collectors.toList());
    }

    public static Map<String, Object> toMap(JsonObject jsonObject) {
        if (jsonObject == null)
            return null;
        Map<String, Object> map = new LinkedHashMap<>();
        for (Entry<String, JsonValue> entry : jsonObject.entrySet())
            map.put(entry.getKey(), toValue(entry.getValue()));
        return map;
    }

    public static boolean isListOf(JsonValue jsonValue, String typename) {
        return jsonValue.getValueType() == ARRAY &&
                jsonValue.asJsonArray().size() > 0 &&
                jsonValue.asJsonArray().get(0).getValueType() == OBJECT &&
                typename.equals(jsonValue.asJsonArray().get(0).asJsonObject().getString("__typename", null));
    }
}
