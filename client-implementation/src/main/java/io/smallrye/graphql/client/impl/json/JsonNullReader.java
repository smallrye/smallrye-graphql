package io.smallrye.graphql.client.impl.json;

import static io.smallrye.graphql.client.impl.json.GraphQlClientValueException.check;

import javax.json.JsonValue;

import io.smallrye.graphql.client.impl.reflection.TypeInfo;

class JsonNullReader extends Reader<JsonValue> {
    JsonNullReader(TypeInfo type, Location location, JsonValue value) {
        super(type, location, value);
    }

    @Override
    Object read() {
        check(location, value, !type.isPrimitive());
        return null;
    }
}
