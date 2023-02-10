package io.smallrye.graphql.client.impl.typesafe.json;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

import jakarta.json.JsonNumber;

import io.smallrye.graphql.client.impl.typesafe.reflection.FieldInfo;
import io.smallrye.graphql.client.impl.typesafe.reflection.TypeInfo;

class JsonNumberReader extends Reader<JsonNumber> {
    JsonNumberReader(TypeInfo type, Location location, JsonNumber value, FieldInfo field) {
        super(type, location, value, field);
    }

    @Override
    Object read() {
        try {
            return read(location, value, type.getRawType());
        } catch (ArithmeticException e) {
            throw GraphQLClientValueHelper.fail(location, value);
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
        if (BigDecimal.class.equals(rawType) || Object.class.equals(rawType))
            return value.bigDecimalValue();
        if (OptionalInt.class.equals(rawType))
            return OptionalInt.of(value.intValueExact());
        if (OptionalLong.class.equals(rawType))
            return OptionalLong.of(value.longValueExact());
        if (OptionalDouble.class.equals(rawType))
            return OptionalDouble.of(value.doubleValue());

        throw GraphQLClientValueHelper.fail(location, value);
    }

    private int readIntBetween(Location location, JsonNumber value, int minValue, int maxValue) {
        int intValue = value.intValueExact();
        GraphQLClientValueHelper.check(location, value, intValue >= minValue);
        GraphQLClientValueHelper.check(location, value, intValue <= maxValue);
        return intValue;
    }
}
