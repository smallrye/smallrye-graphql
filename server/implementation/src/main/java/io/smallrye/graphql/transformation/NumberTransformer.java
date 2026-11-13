package io.smallrye.graphql.transformation;

import static io.smallrye.graphql.SmallRyeGraphQLServerMessages.msg;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import io.smallrye.graphql.scalar.number.BigIntegerScalar;
import io.smallrye.graphql.schema.model.Field;

/**
 * Converts between number-types.
 */
public class NumberTransformer implements Transformer<Number, Number> {

    private static final double LONG_UPPER_BOUND_EXCLUSIVE = -(double) Long.MIN_VALUE;

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
            return input instanceof Integer ? input : intValueExact(input);
        } else if (typeClassName.equals(short.class.getName()) || typeClassName.equals(Short.class.getName())) {
            return input instanceof Short ? input : shortValueExact(input);
        } else if (typeClassName.equals(byte.class.getName()) || typeClassName.equals(Byte.class.getName())) {
            return input instanceof Byte ? input : byteValueExact(input);
            // Float
        } else if (typeClassName.equals(float.class.getName()) || typeClassName.equals(Float.class.getName())) {
            float result = floatValueFinite(input);
            return input instanceof Float ? input : result;
        } else if (typeClassName.equals(double.class.getName()) || typeClassName.equals(Double.class.getName())) {
            double result = doubleValueFinite(input);
            return input instanceof Double ? input : result;

            // BigInteger
        } else if (typeClassName.equals(BigInteger.class.getName())) {
            return bigIntegerValueExact(input);
        } else if (typeClassName.equals(long.class.getName()) || typeClassName.equals(Long.class.getName())) {
            return input instanceof Long ? input : longValueExact(input);
        } else if (typeClassName.equals(AtomicInteger.class.getName())) {
            return input instanceof AtomicInteger ? input : new AtomicInteger(intValueExact(input));
        } else if (typeClassName.equals(AtomicLong.class.getName())) {
            return input instanceof AtomicLong ? input : new AtomicLong(longValueExact(input));

            // BigDecimal
        } else if (typeClassName.equals(BigDecimal.class.getName())) {
            return bigDecimalValue(input);
        }

        throw msg.notAValidNumberType(typeClassName);
    }

    public Number out(final Number object) {
        return object;
    }

    public static int intValueExact(Number value) {
        if (value instanceof BigInteger integer) {
            return integer.intValueExact();
        } else if (value instanceof BigDecimal decimal) {
            return decimal.intValueExact();
        } else if (value instanceof Float || value instanceof Double) {
            return intValueExact(value.doubleValue());
        }
        return Math.toIntExact(longValueExact(value));
    }

    public static int intValueExact(double value) {
        int result = (int) value;
        if (value != result) {
            throw new ArithmeticException("Number is not an exact int: " + value);
        }
        return result;
    }

    public static byte byteValueExact(Number value) {
        int result = intValueExact(value);
        if (result < Byte.MIN_VALUE || result > Byte.MAX_VALUE) {
            throw new ArithmeticException("Number is outside byte range: " + value);
        }
        return (byte) result;
    }

    public static short shortValueExact(Number value) {
        int result = intValueExact(value);
        if (result < Short.MIN_VALUE || result > Short.MAX_VALUE) {
            throw new ArithmeticException("Number is outside short range: " + value);
        }
        return (short) result;
    }

    public static long longValueExact(Number value) {
        if (value instanceof BigInteger integer) {
            return integer.longValueExact();
        } else if (value instanceof BigDecimal decimal) {
            return decimal.longValueExact();
        } else if (value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Long
                || value instanceof AtomicInteger || value instanceof AtomicLong) {
            return value.longValue();
        } else if (value instanceof Float || value instanceof Double) {
            return longValueExact(value.doubleValue());
        }
        throw new ArithmeticException("Unsupported integral number type: " + value.getClass().getName());
    }

    public static long longValueExact(double value) {
        long result = (long) value;
        // Long.MAX_VALUE rounds up to 2^63 as a double, so that upper bound is exclusive.
        if (value >= LONG_UPPER_BOUND_EXCLUSIVE || value != result) {
            throw new ArithmeticException("Number is not an exact long: " + value);
        }
        return result;
    }

    public static float floatValueFinite(Number value) {
        return finiteFloat(value.floatValue());
    }

    public static float finiteFloat(float value) {
        if (!Float.isFinite(value)) {
            throw new ArithmeticException("Number is outside finite float range: " + value);
        }
        return value;
    }

    public static double doubleValueFinite(Number value) {
        return finiteDouble(value.doubleValue());
    }

    public static double finiteDouble(double value) {
        if (!Double.isFinite(value)) {
            throw new ArithmeticException("Number is outside finite double range: " + value);
        }
        return value;
    }

    public static BigInteger bigIntegerValueExact(Number value) {
        if (value instanceof BigInteger integer) {
            return integer;
        } else if (value instanceof BigDecimal decimal) {
            return BigIntegerScalar.toBigIntegerExact(decimal);
        } else if (value instanceof Float || value instanceof Double) {
            return BigIntegerScalar.toBigIntegerExact(new BigDecimal(doubleValueFinite(value)));
        }
        return BigInteger.valueOf(longValueExact(value));
    }

    public static BigDecimal bigDecimalValue(Number value) {
        if (value instanceof BigDecimal decimal) {
            return decimal;
        } else if (value instanceof BigInteger integer) {
            return new BigDecimal(integer);
        } else if (value instanceof Float number) {
            return bigDecimalValue(number.floatValue());
        } else if (value instanceof Double number) {
            return bigDecimalValue(number.doubleValue());
        }
        return BigDecimal.valueOf(longValueExact(value));
    }

    public static BigDecimal bigDecimalValue(float value) {
        finiteFloat(value);
        // Float.toString is the shortest decimal identifying the float; the widened double would expose binary noise
        // (0.1f -> 0.10000000149011612). Integral values keep scale 0 instead of the exponent form.
        return value == Math.rint(value) ? new BigDecimal(value) : new BigDecimal(Float.toString(value));
    }

    public static BigDecimal bigDecimalValue(double value) {
        finiteDouble(value);
        // Integral values keep scale 0 instead of the exponent form of their shortest decimal representation
        return value == Math.rint(value) ? new BigDecimal(value) : BigDecimal.valueOf(value);
    }
}
