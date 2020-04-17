package io.smallrye.graphql.client.impl.json;

import java.util.Optional;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;

import io.smallrye.graphql.client.api.GraphQlClientException;
import io.smallrye.graphql.client.impl.reflection.MethodInfo;
import io.smallrye.graphql.client.impl.reflection.TypeInfo;

public class JsonReader extends Reader<JsonValue> {
    public static Object readFrom(MethodInfo method, JsonValue value) {
        TypeInfo type = method.getReturnType();
        return readJson(new Location(type, method.toString()), type, value);
    }

    static Object readJson(Location location, TypeInfo type, JsonValue value) {
        return new JsonReader(type, location, value).read();
    }

    JsonReader(TypeInfo type, Location location, JsonValue value) {
        super(type, location, value);
    }

    @Override
    Object read() {
        if (type.isOptional())
            return Optional.ofNullable(readJson(location, type.getItemType(), value));
        return reader(location).read();
    }

    private Reader<?> reader(Location location) {
        switch (value.getValueType()) {
            case ARRAY:
                return new JsonArrayReader(type, location, (JsonArray) value);
            case OBJECT:
                return new JsonObjectReader(type, location, (JsonObject) value);
            case STRING:
                return new JsonStringReader(type, location, (JsonString) value);
            case NUMBER:
                return new JsonNumberReader(type, location, (JsonNumber) value);
            case TRUE:
            case FALSE:
                return new JsonBooleanReader(type, location, value);
            case NULL:
                return new JsonNullReader(type, location, value);
        }
        throw new GraphQlClientException("unreachable code");
    }
}
