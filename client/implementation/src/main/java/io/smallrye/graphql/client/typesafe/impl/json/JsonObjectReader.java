package io.smallrye.graphql.client.typesafe.impl.json;

import static io.smallrye.graphql.client.typesafe.impl.json.GraphQLClientValueHelper.check;
import static io.smallrye.graphql.client.typesafe.impl.json.JsonReader.readJson;
import static io.smallrye.graphql.client.typesafe.impl.json.JsonUtils.toMap;

import java.util.Map;

import javax.json.JsonObject;
import javax.json.JsonValue;

import io.smallrye.graphql.client.InvalidResponseException;
import io.smallrye.graphql.client.SmallRyeGraphQLClientMessages;
import io.smallrye.graphql.client.typesafe.impl.reflection.FieldInfo;
import io.smallrye.graphql.client.typesafe.impl.reflection.TypeInfo;

class JsonObjectReader extends Reader<JsonObject> {
    JsonObjectReader(TypeInfo type, Location location, JsonObject value, FieldInfo field) {
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
        if (!type.isRecord()) {
            Object instance = newInstance();
            type.fields().forEach(field -> {
                Object fieldValue = buildValue(location, value, field);
                field.set(instance, fieldValue);
            });
            return instance;
        } else {
            Object[] values = type.fields().map(field -> buildValue(location, value, field)).toArray(Object[]::new);
            return newInstance(values);
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

    private Object buildValue(Location location, JsonObject value, FieldInfo field) {
        String fieldName = field.getAlias().orElseGet(field::getName);
        Location fieldLocation = new Location(field.getType(), location.getDescription() + "." + fieldName);
        JsonValue jsonFieldValue = value.get(fieldName);
        if (jsonFieldValue == null) {
            if (field.isNonNull())
                throw new InvalidResponseException("missing " + fieldLocation);
            return null;
        }
        return readJson(fieldLocation, field.getType(), jsonFieldValue, field);
    }
}
