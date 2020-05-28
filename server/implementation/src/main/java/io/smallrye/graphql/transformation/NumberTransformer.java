package io.smallrye.graphql.transformation;

import java.math.BigDecimal;
import java.math.BigInteger;

import io.smallrye.graphql.SmallRyeGraphQLServerMessages;
import io.smallrye.graphql.schema.model.Field;

/**
 * Converts between number-types.
 */
public class NumberTransformer implements Transformer<Number, Number> {

    private final String typeClassName;

    protected NumberTransformer(Field field) {
        this(field.getReference().getClassName());
    }

    public NumberTransformer(String targetClassName) {
        this.typeClassName = targetClassName;
    }

    @Override
    public Number in(final Number input) {
        // Integer
        if (typeClassName.equals(int.class.getName()) || typeClassName.equals(Integer.class.getName())) {
            return input.intValue();
        } else if (typeClassName.equals(short.class.getName()) || typeClassName.equals(Short.class.getName())) {
            return (short) input.intValue();
        } else if (typeClassName.equals(byte.class.getName()) || typeClassName.equals(Byte.class.getName())) {
            return (byte) input.intValue();
            // Float
        } else if (typeClassName.equals(float.class.getName())) {
            return input.floatValue();
        } else if (typeClassName.equals(Float.class.getName())) {
            return input.floatValue();
        } else if (typeClassName.equals(double.class.getName())) {
            return input.doubleValue();
        } else if (typeClassName.equals(Double.class.getName())) {
            return input.doubleValue();

            // BigInteger
        } else if (typeClassName.equals(BigInteger.class.getName())) {
            if (input instanceof BigDecimal) {
                return ((BigDecimal) input).toBigInteger();
            }
            if (input instanceof BigInteger) {
                return input;
            }
            return BigInteger.valueOf(input.longValue());
        } else if (typeClassName.equals(long.class.getName())) {
            return input.longValue();
        } else if (typeClassName.equals(Long.class.getName())) {
            return input.longValue();

            // BigDecimal
        } else if (typeClassName.equals(BigDecimal.class.getName())) {
            if (input instanceof BigDecimal) {
                return (input);
            }
            if (input instanceof BigInteger) {
                return new BigDecimal(((BigInteger) input));
            }
            return BigDecimal.valueOf(input.doubleValue());
        }

        throw SmallRyeGraphQLServerMessages.msg.notAValidNumberType(typeClassName);
    }

    public Number out(final Number object) {
        return object;
    }
}
