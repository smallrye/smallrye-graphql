package io.smallrye.graphql.transformation;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;

import io.smallrye.graphql.schema.model.Field;

/**
 * Parses incoming numbers to the needed class, doesn't touch outgoing values.
 */
public class NumberTransformer implements Transformer {

    private final Field field;

    protected NumberTransformer(Field field) {
        this.field = field;
    }

    @Override
    public Object in(final Object o) throws ParseException {
        return parseNumber(o.toString(), field.getReference().getClassName());
    }

    public Number parseNumber(String input, String typeClassName) throws ParseException {
        // Integer
        if (typeClassName.equals(int.class.getName())) {
            return (Integer.parseInt(input));
        } else if (typeClassName.equals(Integer.class.getName())) {
            return (Integer.valueOf(input));
        } else if (typeClassName.equals(short.class.getName())) {
            return (Short.parseShort(input));
        } else if (typeClassName.equals(Short.class.getName())) {
            return (Short.valueOf(input));
        } else if (typeClassName.equals(byte.class.getName())) {
            return (Byte.parseByte(input));
        } else if (typeClassName.equals(Byte.class.getName())) {
            return (Byte.valueOf(input));

            // Float
        } else if (typeClassName.equals(float.class.getName())) {
            return (Float.parseFloat(input));
        } else if (typeClassName.equals(Float.class.getName())) {
            return (Float.valueOf(input));
        } else if (typeClassName.equals(double.class.getName())) {
            return (Double.parseDouble(input));
        } else if (typeClassName.equals(Double.class.getName())) {
            return (Double.valueOf(input));

            // BigInteger
        } else if (typeClassName.equals(BigInteger.class.getName())) {
            return (new BigInteger(input));
        } else if (typeClassName.equals(long.class.getName())) {
            return (Long.parseLong(input));
        } else if (typeClassName.equals(Long.class.getName())) {
            return (Long.valueOf(input));

            // BigDecimal
        } else if (typeClassName.equals(BigDecimal.class.getName())) {
            return (new BigDecimal(input));
        }

        throw new RuntimeException(String.format("[%s] is no valid number-type", typeClassName));
    }

    public Object out(final Object object) {
        return object;
    }
}
