package io.smallrye.graphql.client.typesafe.impl.json;

import static io.smallrye.graphql.client.typesafe.impl.json.GraphQlClientValueException.check;

import javax.json.JsonValue;

import io.smallrye.graphql.client.typesafe.impl.reflection.TypeInfo;

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
