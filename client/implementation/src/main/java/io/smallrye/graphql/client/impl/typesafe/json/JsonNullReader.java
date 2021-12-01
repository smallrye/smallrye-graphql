package io.smallrye.graphql.client.impl.typesafe.json;

import static io.smallrye.graphql.client.impl.typesafe.json.GraphQLClientValueHelper.check;

import javax.json.JsonValue;

import io.smallrye.graphql.client.impl.typesafe.reflection.FieldInfo;
import io.smallrye.graphql.client.impl.typesafe.reflection.TypeInfo;

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
