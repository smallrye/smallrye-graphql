package io.smallrye.graphql.bootstrap;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import graphql.language.ArrayValue;
import graphql.language.BooleanValue;
import graphql.language.EnumValue;
import graphql.language.FloatValue;
import graphql.language.IntValue;
import graphql.language.NullValue;
import graphql.language.ObjectField;
import graphql.language.ObjectValue;
import graphql.language.StringValue;
import graphql.language.Value;

/**
 * Helper class to convert Java objects to GraphQL AST Value objects
 * for schema printing purposes.
 *
 * This is used to render default values in the schema when using
 * defaultValueLiteral() on GraphQL argument and input field builders.
 */
public class DefaultValueAstHelper {

    /**
     * Convert a Java object (Map, List, String, Number, Boolean, etc.) to GraphQL AST Value
     * for schema printing purposes.
     *
     * @param value the Java object to convert
     * @return GraphQL AST Value representation
     */
    public static Value<?> toAstValue(Object value) {
        if (value == null) {
            return NullValue.newNullValue().build();
        }

        if (value instanceof Map) {
            return toObjectValue((Map<?, ?>) value);
        } else if (value instanceof List) {
            return toArrayValue((List<?>) value);
        } else if (value.getClass().isArray()) {
            // Handle Java arrays (String[], int[], etc.)
            return toArrayValueFromArray(value);
        } else if (value instanceof String) {
            return StringValue.newStringValue((String) value).build();
        } else if (value instanceof Boolean) {
            return BooleanValue.newBooleanValue((Boolean) value).build();
        } else if (value instanceof Number) {
            // Handle Int, Float, BigDecimal, etc.
            return toNumericValue((Number) value);
        } else if (value instanceof Enum) {
            return EnumValue.newEnumValue(((Enum<?>) value).name()).build();
        }

        // Fallback: convert to string
        return StringValue.newStringValue(value.toString()).build();
    }

    private static ObjectValue toObjectValue(Map<?, ?> map) {
        ObjectValue.Builder builder = ObjectValue.newObjectValue();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            String key = entry.getKey().toString();
            Value<?> val = toAstValue(entry.getValue());
            builder.objectField(ObjectField.newObjectField()
                    .name(key)
                    .value(val)
                    .build());
        }
        return builder.build();
    }

    private static ArrayValue toArrayValue(List<?> list) {
        ArrayValue.Builder builder = ArrayValue.newArrayValue();
        for (Object item : list) {
            builder.value(toAstValue(item));
        }
        return builder.build();
    }

    private static ArrayValue toArrayValueFromArray(Object array) {
        ArrayValue.Builder builder = ArrayValue.newArrayValue();
        int length = java.lang.reflect.Array.getLength(array);
        for (int i = 0; i < length; i++) {
            Object item = java.lang.reflect.Array.get(array, i);
            builder.value(toAstValue(item));
        }
        return builder.build();
    }

    private static Value<?> toNumericValue(Number number) {
        // Check if it's an integer type or if the value has no fractional part
        if (number instanceof Integer || number instanceof Long ||
                number instanceof Short || number instanceof Byte) {
            return IntValue.newIntValue(BigInteger.valueOf(number.longValue())).build();
        }

        // For floating point types (Float, Double, BigDecimal), check if they represent whole numbers
        double doubleValue = number.doubleValue();
        if (doubleValue == Math.floor(doubleValue) && !Double.isInfinite(doubleValue)) {
            // It's a whole number, treat as Int
            return IntValue.newIntValue(BigInteger.valueOf(number.longValue())).build();
        }

        // Has fractional part, treat as float
        return FloatValue.newFloatValue(BigDecimal.valueOf(doubleValue)).build();
    }
}
