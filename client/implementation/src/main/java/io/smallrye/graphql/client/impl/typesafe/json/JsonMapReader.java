package io.smallrye.graphql.client.impl.typesafe.json;

import java.util.HashMap;
import java.util.Map;

import javax.json.JsonArray;
import javax.json.JsonValue;

import io.smallrye.graphql.client.InvalidResponseException;
import io.smallrye.graphql.client.impl.typesafe.reflection.FieldInfo;
import io.smallrye.graphql.client.impl.typesafe.reflection.TypeInfo;

class JsonMapReader extends Reader<JsonArray> {

    // these two strings denote the expected keys in the JSON representation of a map entry
    // for example, a single map entry can be serialized as `{"key":2,"value":"b"}` where the `key` and `value`
    // are the strings contained in these two constants
    private static final String JSON_KEY_FOR_KEY = "key";
    private static final String JSON_KEY_FOR_VALUE = "value";

    // type of keys expected to be present in the map
    private final TypeInfo keyType;
    // type of values expected to be present in the map
    private final TypeInfo valueType;

    JsonMapReader(TypeInfo type, Location location, JsonArray value, FieldInfo field) {
        super(type, location, value, field);
        this.keyType = type.getKeyType();
        this.valueType = type.getValueType();
    }

    @Override
    Object read() {
        GraphQLClientValueHelper.check(location, value, type.isMap());
        MapLocationBuilder locationBuilder = new MapLocationBuilder(location);
        Map result = new HashMap<>();
        for (JsonValue entry : value) {
            Location keyLocation = locationBuilder.nextKeyLocation();
            Location valueLocation = locationBuilder.nextValueLocation();

            JsonValue keyJson = entry.asJsonObject().get(JSON_KEY_FOR_KEY);
            if (keyJson.getValueType() == JsonValue.ValueType.NULL) {
                throw new InvalidResponseException("unexpected null key at " + keyLocation);
            }
            JsonValue valueJson = entry.asJsonObject().get(JSON_KEY_FOR_VALUE);
            if (valueJson.getValueType() == JsonValue.ValueType.NULL && valueType.isNonNull()) {
                throw new InvalidResponseException("unexpected null value at " + keyLocation);
            }

            Object keyDeserialized = JsonReader.readJson(keyLocation, keyType, keyJson, field);
            Object valueDeserialized = JsonReader.readJson(valueLocation, valueType, valueJson, field);

            result.put(keyDeserialized, valueDeserialized);
        }
        return result;
    }

}
