package io.smallrye.graphql.client.typesafe.impl.json;

import java.time.Instant;

import javax.json.JsonString;

import io.smallrye.graphql.client.typesafe.api.GraphQLClientException;
import io.smallrye.graphql.client.typesafe.impl.reflection.ConstructionInfo;
import io.smallrye.graphql.client.typesafe.impl.reflection.TypeInfo;

class JsonStringReader extends Reader<JsonString> {
    JsonStringReader(TypeInfo type, Location location, JsonString value) {
        super(type, location, value);
    }

    @Override
    Object read() {
        if (char.class.equals(type.getRawType()) || Character.class.equals(type.getRawType())) {
            if (value.getChars().length() != 1)
                throw new GraphQLClientValueException(location, value);
            return value.getChars().charAt(0);
        }
        if (String.class.equals(type.getRawType()) || Object.class.equals(type.getRawType())) // TODO CharSequence
            return value.getString();
        if (type.isEnum())
            return enumValue();

        if (java.util.Date.class.equals(this.type.getRawType()))
            return java.util.Date.from(Instant.parse(value.getString()));
        if (java.util.UUID.class.equals(this.type.getRawType()))
            return java.util.UUID.fromString(value.getString());

        ConstructionInfo constructor = type.scalarConstructor()
                .orElseThrow(() -> new GraphQLClientValueException(location, value));
        try {
            return constructor.execute(value.getString());
        } catch (Exception e) {
            throw new GraphQLClientException("can't create scalar " + location, e);
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Enum<?> enumValue() {
        return Enum.valueOf((Class) type.getRawType(), value.getString());
    }
}
