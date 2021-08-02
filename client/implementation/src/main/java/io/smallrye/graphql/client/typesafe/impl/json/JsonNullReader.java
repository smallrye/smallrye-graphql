package io.smallrye.graphql.client.typesafe.impl.json;

import static io.smallrye.graphql.client.typesafe.impl.json.GraphQLClientValueException.check;

import javax.json.JsonValue;

import io.smallrye.graphql.client.typesafe.impl.reflection.FieldInfo;
import io.smallrye.graphql.client.typesafe.impl.reflection.TypeInfo;

class JsonNullReader extends Reader<JsonValue> {
    JsonNullReader(TypeInfo type, Location location, JsonValue value, FieldInfo field) {
        super(type, location, value, field);
    }

    @Override
    Object read() {
        check(location, value, !type.isPrimitive());
        return null;
    }
}
