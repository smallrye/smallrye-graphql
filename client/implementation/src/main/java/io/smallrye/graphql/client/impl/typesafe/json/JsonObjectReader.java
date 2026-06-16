package io.smallrye.graphql.client.impl.typesafe.json;

import static io.smallrye.graphql.client.impl.typesafe.json.GraphQLClientValueHelper.check;
import static io.smallrye.graphql.client.impl.typesafe.json.JsonReader.readJson;
import static io.smallrye.graphql.client.impl.typesafe.json.JsonUtils.toMap;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.smallrye.graphql.client.InvalidResponseException;
import io.smallrye.graphql.client.impl.SmallRyeGraphQLClientMessages;
import io.smallrye.graphql.client.impl.typesafe.reflection.FieldInfo;
import io.smallrye.graphql.client.impl.typesafe.reflection.TypeInfo;

class JsonObjectReader extends Reader<ObjectNode> {
    JsonObjectReader(TypeInfo type, Location location, ObjectNode value, FieldInfo field) {
        super(type, location, value, field);
    }

    @Override
    Object read() {
        check(location, value, !type.isCollection() && !type.isScalar());
        if (Map.class.equals(type.getRawType()))
            return toMap(value);
        return readObject();
    }

    private Object readObject() {
        if (type.isUnion() || type.isInterface()) {
            var subtype = type.subtype(value.get("__typename").asText());
            var instance = subtype.newInstance(new Object[0]);
            subtype.fields().forEach(field -> {
                Object fieldValue = buildValue(location, value, field);
                field.set(instance, fieldValue);
            });
            return instance;
        } else if (type.isRecord()) {
            Object[] values = type.fields().map(field -> buildValue(location, value, field)).toArray(Object[]::new);
            return newInstance(values);
        } else {
            Object instance = newInstance();
            type.fields().forEach(field -> {
                Object fieldValue = buildValue(location, value, field);
                field.set(instance, fieldValue);
            });
            return instance;
        }
    }

    private Object newInstance(Object[] parameters) {
        try {
            return type.newInstance(parameters);
        } catch (Exception e) {
            throw SmallRyeGraphQLClientMessages.msg.cannotInstantiateDomainObject(location.toString(), e);
        }
    }

    private Object newInstance() {
        return newInstance(new Object[0]);
    }

    private Object buildValue(Location location, ObjectNode value, FieldInfo field) {
        String fieldName = field.getAlias().orElseGet(field::getName);
        Location fieldLocation = new Location(field.getType(), location.getDescription() + "." + fieldName);
        JsonNode jsonFieldValue = value.get(fieldName);
        if (jsonFieldValue == null) {
            if (field.isNonNull())
                throw new InvalidResponseException("missing " + fieldLocation);
            return null;
        }
        return readJson(fieldLocation, field.getType(), jsonFieldValue, field);
    }
}
