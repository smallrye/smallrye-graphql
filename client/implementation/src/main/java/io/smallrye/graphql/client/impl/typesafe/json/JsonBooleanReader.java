package io.smallrye.graphql.client.impl.typesafe.json;

import io.smallrye.graphql.client.impl.typesafe.reflection.FieldInfo;
import io.smallrye.graphql.client.impl.typesafe.reflection.TypeInfo;
import tools.jackson.databind.JsonNode;

class JsonBooleanReader extends Reader<JsonNode> {
    JsonBooleanReader(TypeInfo type, Location location, JsonNode value, FieldInfo field) {
        super(type, location, value, field);
    }

    @Override
    Object read() {
        assert value.isBoolean();
        GraphQLClientValueHelper.check(location, value,
                boolean.class.equals(type.getRawType()) || Boolean.class.equals(type.getRawType()));
        return value.booleanValue();
    }
}
