package io.smallrye.graphql.transformation;

import static java.util.Map.entry;

import java.util.Map;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Java numeric types whose range is narrower than the GraphQL scalar carrying their values.
 * Values for these types must be range checked before they are bound, otherwise they silently overflow.
 */
public enum BoundedNumberType {
    BYTE {
        @Override
        public void checkRange(Number value) {
            NumberTransformer.byteValueExact(value);
        }
    },
    SHORT {
        @Override
        public void checkRange(Number value) {
            NumberTransformer.shortValueExact(value);
        }
    },
    INT {
        @Override
        public void checkRange(Number value) {
            NumberTransformer.intValueExact(value);
        }
    },
    LONG {
        @Override
        public void checkRange(Number value) {
            NumberTransformer.longValueExact(value);
        }
    },
    FLOAT {
        @Override
        public void checkRange(Number value) {
            NumberTransformer.floatValueFinite(value);
        }
    },
    DOUBLE {
        @Override
        public void checkRange(Number value) {
            NumberTransformer.doubleValueFinite(value);
        }
    };

    private static final Map<String, BoundedNumberType> BY_CLASS_NAME = Map.ofEntries(
            entry(byte.class.getName(), BYTE),
            entry(Byte.class.getName(), BYTE),
            entry(short.class.getName(), SHORT),
            entry(Short.class.getName(), SHORT),
            entry(int.class.getName(), INT),
            entry(Integer.class.getName(), INT),
            entry(AtomicInteger.class.getName(), INT),
            entry(OptionalInt.class.getName(), INT),
            entry(long.class.getName(), LONG),
            entry(Long.class.getName(), LONG),
            entry(AtomicLong.class.getName(), LONG),
            entry(OptionalLong.class.getName(), LONG),
            entry(float.class.getName(), FLOAT),
            entry(Float.class.getName(), FLOAT),
            entry(double.class.getName(), DOUBLE),
            entry(Double.class.getName(), DOUBLE),
            entry(OptionalDouble.class.getName(), DOUBLE));

    /**
     * @throws ArithmeticException if the value does not fit this type exactly (or, for floating types, is not finite)
     */
    public abstract void checkRange(Number value);

    /**
     * @return the bounded type for the class name, or {@code null} for unbounded (BigInteger, BigDecimal) and
     *         non-numeric types
     */
    public static BoundedNumberType forClassName(String className) {
        return BY_CLASS_NAME.get(className);
    }
}
