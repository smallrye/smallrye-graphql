package io.smallrye.graphql.client.typesafe.impl.json;

import javax.json.JsonString;

import io.smallrye.graphql.client.typesafe.api.GraphQlClientException;
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
                throw new GraphQlClientValueException(location, value);
            return value.getChars().charAt(0);
        }
        if (String.class.equals(type.getRawType()))
            return value.getString();
        if (type.isEnum())
            //noinspection rawtypes,unchecked
            return Enum.valueOf((Class) type.getRawType(), value.getString());

        ConstructionInfo constructor = type.scalarConstructor()
                .orElseThrow(() -> new GraphQlClientValueException(location, value));
        try {
            return constructor.execute(value.getString());
        } catch (Exception e) {
            throw new GraphQlClientException("can't create scalar " + location, e);
        }
    }
}
