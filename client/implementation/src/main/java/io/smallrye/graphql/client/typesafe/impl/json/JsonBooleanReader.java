package io.smallrye.graphql.client.typesafe.impl.json;

import static io.smallrye.graphql.client.typesafe.impl.json.GraphQLClientValueException.check;
import static javax.json.JsonValue.ValueType.FALSE;
import static javax.json.JsonValue.ValueType.TRUE;

import javax.json.JsonValue;

import io.smallrye.graphql.client.typesafe.impl.reflection.FieldInfo;
import io.smallrye.graphql.client.typesafe.impl.reflection.TypeInfo;

class JsonBooleanReader extends Reader<JsonValue> {
    JsonBooleanReader(TypeInfo type, Location location, JsonValue value, FieldInfo field) {
        super(type, location, value, field);
    }

    @Override
    Object read() {
        assert value.getValueType() == TRUE || value.getValueType() == FALSE;
        check(location, value, boolean.class.equals(type.getRawType()) || Boolean.class.equals(type.getRawType()));
        return value.getValueType() == TRUE;
    }
}
