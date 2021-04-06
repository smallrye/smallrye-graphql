package io.smallrye.graphql.client.typesafe.impl.json;

import static io.smallrye.graphql.client.typesafe.impl.json.GraphQlClientValueException.check;
import static io.smallrye.graphql.client.typesafe.impl.json.JsonReader.readJson;
import static io.smallrye.graphql.client.typesafe.impl.json.JsonUtils.toMap;

import java.util.Map;

import javax.json.JsonObject;
import javax.json.JsonValue;

import io.smallrye.graphql.client.typesafe.api.GraphQlClientException;
import io.smallrye.graphql.client.typesafe.impl.reflection.FieldInfo;
import io.smallrye.graphql.client.typesafe.impl.reflection.TypeInfo;

class JsonObjectReader extends Reader<JsonObject> {
    JsonObjectReader(TypeInfo type, Location location, JsonObject value) {
        super(type, location, value);
    }

    @Override
    Object read() {
        check(location, value, !type.isCollection() && !type.isScalar());
        if (Map.class.equals(type.getRawType()))
            return toMap(value);
        return readObject();
    }

    private Object readObject() {
        Object instance = newInstance();
        type.fields().forEach(field -> {
            Object fieldValue = buildValue(location, value, field);
            field.set(instance, fieldValue);
        });
        return instance;
    }

    private Object newInstance() {
        try {
            return type.newInstance();
        } catch (Exception e) {
            throw new GraphQlClientException("can't create " + location, e);
        }
    }

    private Object buildValue(Location location, JsonObject value, FieldInfo field) {
        String fieldName = field.getAlias().orElseGet(field::getName);
        Location fieldLocation = new Location(field.getType(), location.getDescription() + "." + fieldName);
        JsonValue jsonFieldValue = value.get(fieldName);
        if (jsonFieldValue == null) {
            if (field.isNonNull())
                throw new GraphQlClientException("missing " + fieldLocation);
            return null;
        }
        return readJson(fieldLocation, field.getType(), jsonFieldValue);
    }
}
