package io.smallrye.graphql.client.typesafe.impl.json;

import static io.smallrye.graphql.client.typesafe.impl.json.GraphQLClientValueException.check;

import java.math.BigDecimal;
import java.math.BigInteger;

import javax.json.JsonNumber;

import io.smallrye.graphql.client.typesafe.impl.reflection.TypeInfo;

class JsonNumberReader extends Reader<JsonNumber> {
    JsonNumberReader(TypeInfo type, Location location, JsonNumber value) {
        super(type, location, value);
    }

    @Override
    Object read() {
        try {
            return read(location, value, type.getRawType());
        } catch (ArithmeticException e) {
            throw new GraphQLClientValueException(location, value, e);
        }
    }

    private Object read(Location location, JsonNumber value, Class<?> rawType) {
        if (byte.class.equals(rawType) || Byte.class.equals(rawType))
            return (byte) readIntBetween(location, value, Byte.MIN_VALUE, Byte.MAX_VALUE);
        if (char.class.equals(rawType) || Character.class.equals(rawType))
            return (char) readIntBetween(location, value, Character.MIN_VALUE, Character.MAX_VALUE);
        if (short.class.equals(rawType) || Short.class.equals(rawType))
            return (short) readIntBetween(location, value, Short.MIN_VALUE, Short.MAX_VALUE);
        if (int.class.equals(rawType) || Integer.class.equals(rawType))
            return value.intValueExact();
        if (long.class.equals(rawType) || Long.class.equals(rawType))
            return value.longValueExact();
        if (float.class.equals(rawType) || Float.class.equals(rawType))
            return (float) value.doubleValue();
        if (double.class.equals(rawType) || Double.class.equals(rawType))
            return value.doubleValue();
        if (BigInteger.class.equals(rawType))
            return value.bigIntegerValueExact();
        if (BigDecimal.class.equals(rawType))
            return value.bigDecimalValue();

        throw new GraphQLClientValueException(location, value);
    }

    private int readIntBetween(Location location, JsonNumber value, int minValue, int maxValue) {
        int intValue = value.intValueExact();
        check(location, value, intValue >= minValue);
        check(location, value, intValue <= maxValue);
        return intValue;
    }
}
