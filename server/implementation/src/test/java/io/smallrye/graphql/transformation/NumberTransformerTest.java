package io.smallrye.graphql.transformation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class NumberTransformerTest {

    @ParameterizedTest
    @ValueSource(strings = { "int", "java.lang.Integer" })
    void integerConversionPreservesBoundsAndRejectsOverflow(String target) {
        NumberTransformer transformer = new NumberTransformer(target);
        assertEquals(Integer.MIN_VALUE, transformer.in(new BigInteger("-2147483648")));
        assertEquals(Integer.MAX_VALUE, transformer.in(new BigDecimal("2147483647.0")));
        assertThrows(ArithmeticException.class, () -> transformer.in(new BigInteger("2147483648")));
        assertThrows(ArithmeticException.class, () -> transformer.in(new BigInteger("-2147483649")));
        assertThrows(ArithmeticException.class, () -> transformer.in(new BigDecimal("2147483648")));
        assertThrows(ArithmeticException.class, () -> transformer.in(new BigDecimal("-2147483649")));
        assertThrows(ArithmeticException.class, () -> transformer.in(new BigDecimal("1.5")));
        assertThrows(ArithmeticException.class, () -> transformer.in(2147483648L));
        assertThrows(ArithmeticException.class, () -> transformer.in(new AtomicLong(-2147483649L)));
    }

    @ParameterizedTest
    @ValueSource(strings = { "long", "java.lang.Long" })
    void longConversionPreservesBoundsAndRejectsOverflow(String target) {
        NumberTransformer transformer = new NumberTransformer(target);
        assertEquals(Long.MIN_VALUE, transformer.in(new BigInteger("-9223372036854775808")));
        assertEquals(Long.MAX_VALUE, transformer.in(new BigDecimal("9223372036854775807.0")));
        assertThrows(ArithmeticException.class, () -> transformer.in(new BigInteger("9223372036854775808")));
        assertThrows(ArithmeticException.class, () -> transformer.in(new BigInteger("-9223372036854775809")));
        assertThrows(ArithmeticException.class, () -> transformer.in(new BigDecimal("9223372036854775808")));
        assertThrows(ArithmeticException.class, () -> transformer.in(new BigDecimal("-9223372036854775809")));
        assertThrows(ArithmeticException.class, () -> transformer.in(new BigDecimal("1.5")));
    }

    @ParameterizedTest
    @ValueSource(strings = { "int", "java.lang.Integer", "long", "java.lang.Long" })
    void ordinaryIntegralNumbersStillConvert(String target) {
        NumberTransformer transformer = new NumberTransformer(target);
        Number[] inputs = { (byte) 42, (short) 42, 42, 42L, new AtomicInteger(42), new AtomicLong(42) };
        for (Number input : inputs) {
            assertEquals(42L, transformer.in(input).longValue());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "int", "java.lang.Integer", "long", "java.lang.Long" })
    void floatingPointInputsMustBeWholeAndInRange(String target) {
        NumberTransformer transformer = new NumberTransformer(target);
        assertEquals(42L, transformer.in(42.0d).longValue());
        assertEquals(42L, transformer.in(42.0f).longValue());
        assertThrows(ArithmeticException.class, () -> transformer.in(1.5d));
        assertThrows(ArithmeticException.class, () -> transformer.in(Double.NaN));
        assertThrows(ArithmeticException.class, () -> transformer.in(Double.POSITIVE_INFINITY));
        assertThrows(ArithmeticException.class, () -> transformer.in(Double.NEGATIVE_INFINITY));
        assertThrows(ArithmeticException.class, () -> transformer.in(0x1p63));
    }
}
