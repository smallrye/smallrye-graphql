package io.smallrye.graphql.client.impl.typesafe.json;

import static jakarta.json.JsonValue.ValueType.FALSE;
import static jakarta.json.JsonValue.ValueType.TRUE;

import jakarta.json.JsonValue;

import io.smallrye.graphql.client.impl.typesafe.reflection.FieldInfo;
import io.smallrye.graphql.client.impl.typesafe.reflection.TypeInfo;

class JsonBooleanReader extends Reader<JsonValue> {
    JsonBooleanReader(TypeInfo type, Location location, JsonValue value, FieldInfo field) {
        super(type, location, value, field);
    }

    @Override
    Object read() {
        assert value.getValueType() == TRUE || value.getValueType() == FALSE;
        GraphQLClientValueHelper.check(location, value,
                boolean.class.equals(type.getRawType()) || Boolean.class.equals(type.getRawType()));
        return value.getValueType() == TRUE;
    }
}
