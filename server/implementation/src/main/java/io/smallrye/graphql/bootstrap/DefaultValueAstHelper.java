package io.smallrye.graphql.bootstrap;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;

import org.jboss.logging.Logger;

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
 * Converts Java objects to GraphQL AST {@link Value} nodes for schema printing.
 * <p>
 * graphql-java's {@code defaultValueProgrammatic()} handles runtime behavior but does not
 * render default values in printed SDL or introspection; {@code defaultValueLiteral()} with
 * an AST Value is needed for that. This helper bridges the two representations.
 */
public class DefaultValueAstHelper {

    private static final Logger LOG = Logger.getLogger(DefaultValueAstHelper.class);

    private DefaultValueAstHelper() {
    }

    public static Value<?> toAstValue(Object value) {
        if (value == null) {
            return NullValue.newNullValue().build();
        }

        if (value instanceof Map) {
            return toObjectValue((Map<?, ?>) value);
        } else if (value instanceof Collection) {
            return toCollectionValue((Collection<?>) value);
        } else if (value.getClass().isArray()) {
            return toArrayValue(value);
        } else if (value instanceof String) {
            return StringValue.newStringValue((String) value).build();
        } else if (value instanceof Boolean) {
            return BooleanValue.newBooleanValue((Boolean) value).build();
        } else if (value instanceof Number) {
            return toNumericValue((Number) value);
        } else if (value instanceof Enum) {
            return EnumValue.newEnumValue(((Enum<?>) value).name()).build();
        }

        LOG.warnf("Unrecognized default value type for schema printing: %s", value.getClass().getName());
        return StringValue.newStringValue(value.toString()).build();
    }

    private static ObjectValue toObjectValue(Map<?, ?> map) {
        ObjectValue.Builder builder = ObjectValue.newObjectValue();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            builder.objectField(ObjectField.newObjectField()
                    .name(entry.getKey().toString())
                    .value(toAstValue(entry.getValue()))
                    .build());
        }
        return builder.build();
    }

    private static ArrayValue toCollectionValue(Collection<?> collection) {
        ArrayValue.Builder builder = ArrayValue.newArrayValue();
        for (Object item : collection) {
            builder.value(toAstValue(item));
        }
        return builder.build();
    }

    private static ArrayValue toArrayValue(Object array) {
        ArrayValue.Builder builder = ArrayValue.newArrayValue();
        int length = java.lang.reflect.Array.getLength(array);
        for (int i = 0; i < length; i++) {
            builder.value(toAstValue(java.lang.reflect.Array.get(array, i)));
        }
        return builder.build();
    }

    private static Value<?> toNumericValue(Number number) {
        if (number instanceof BigInteger) {
            return IntValue.newIntValue((BigInteger) number).build();
        }
        if (number instanceof BigDecimal) {
            BigDecimal bd = (BigDecimal) number;
            if (bd.scale() <= 0) {
                return IntValue.newIntValue(bd.toBigInteger()).build();
            }
            return FloatValue.newFloatValue(bd).build();
        }
        if (number instanceof Integer || number instanceof Long
                || number instanceof Short || number instanceof Byte) {
            return IntValue.newIntValue(BigInteger.valueOf(number.longValue())).build();
        }
        return FloatValue.newFloatValue(BigDecimal.valueOf(number.doubleValue())).build();
    }
}
