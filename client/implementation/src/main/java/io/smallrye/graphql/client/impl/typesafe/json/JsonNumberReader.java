package io.smallrye.graphql.client.impl.typesafe.json;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

import com.fasterxml.jackson.databind.JsonNode;

import io.smallrye.graphql.client.impl.typesafe.reflection.FieldInfo;
import io.smallrye.graphql.client.impl.typesafe.reflection.TypeInfo;

class JsonNumberReader extends Reader<JsonNode> {
    JsonNumberReader(TypeInfo type, Location location, JsonNode value, FieldInfo field) {
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

    private Object read(Location location, JsonNode value, Class<?> rawType) {
        if (byte.class.equals(rawType) || Byte.class.equals(rawType))
            return (byte) readIntBetween(location, value, Byte.MIN_VALUE, Byte.MAX_VALUE);
        if (char.class.equals(rawType) || Character.class.equals(rawType))
            return (char) readIntBetween(location, value, Character.MIN_VALUE, Character.MAX_VALUE);
        if (short.class.equals(rawType) || Short.class.equals(rawType))
            return (short) readIntBetween(location, value, Short.MIN_VALUE, Short.MAX_VALUE);
        if (int.class.equals(rawType) || Integer.class.equals(rawType))
            return readIntExact(location, value);
        if (long.class.equals(rawType) || Long.class.equals(rawType))
            return readLongExact(location, value);
        if (float.class.equals(rawType) || Float.class.equals(rawType))
            return (float) value.doubleValue();
        if (double.class.equals(rawType) || Double.class.equals(rawType))
            return value.doubleValue();
        if (BigInteger.class.equals(rawType))
            return value.bigIntegerValue();
        if (BigDecimal.class.equals(rawType) || Object.class.equals(rawType))
            return value.decimalValue();
        if (OptionalInt.class.equals(rawType))
            return OptionalInt.of(readIntExact(location, value));
        if (OptionalLong.class.equals(rawType))
            return OptionalLong.of(readLongExact(location, value));
        if (OptionalDouble.class.equals(rawType))
            return OptionalDouble.of(value.doubleValue());

        throw GraphQLClientValueHelper.fail(location, value);
    }

    private int readIntExact(Location location, JsonNode value) {
        if (!value.canConvertToInt()) {
            throw GraphQLClientValueHelper.fail(location, value);
        }
        return value.intValue();
    }

    private long readLongExact(Location location, JsonNode value) {
        if (!value.canConvertToLong()) {
            throw GraphQLClientValueHelper.fail(location, value);
        }
        return value.longValue();
    }

    private int readIntBetween(Location location, JsonNode value, int minValue, int maxValue) {
        int intValue = readIntExact(location, value);
        GraphQLClientValueHelper.check(location, value, intValue >= minValue);
        GraphQLClientValueHelper.check(location, value, intValue <= maxValue);
        return intValue;
    }
}
