package io.smallrye.graphql.client.impl.typesafe.json;

import static io.smallrye.graphql.client.impl.typesafe.json.GraphQLClientValueHelper.check;

import io.smallrye.graphql.client.impl.typesafe.reflection.FieldInfo;
import io.smallrye.graphql.client.impl.typesafe.reflection.TypeInfo;
import tools.jackson.databind.JsonNode;

class JsonNullReader extends Reader<JsonNode> {
    JsonNullReader(TypeInfo type, Location location, JsonNode value, FieldInfo field) {
        super(type, location, value, field);
    }

    @Override
    Object read() {
        if (!type.isVoid()) {
            check(location, value, !type.isPrimitive());
        }
        return null;
    }
}
