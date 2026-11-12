package io.smallrye.graphql.transformation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class NumericConversionCoverageTest {

    static Stream<Arguments> primitiveAndBoxedTargets() {
        return Stream.of(
                Arguments.of("byte", (byte) 42), Arguments.of(Byte.class.getName(), (byte) 42),
                Arguments.of("short", (short) 42), Arguments.of(Short.class.getName(), (short) 42),
                Arguments.of("int", 42), Arguments.of(Integer.class.getName(), 42),
                Arguments.of("long", 42L), Arguments.of(Long.class.getName(), 42L),
                Arguments.of("float", 42.0f), Arguments.of(Float.class.getName(), 42.0f),
                Arguments.of("double", 42.0d), Arguments.of(Double.class.getName(), 42.0d));
    }

    @ParameterizedTest
    @MethodSource("primitiveAndBoxedTargets")
    void convertsWholeNumbersToTheRequestedPrimitiveOrBoxedType(String target, Number expected) {
        NumberTransformer transformer = new NumberTransformer(target);
        Number[] inputs = { (byte) 42, (short) 42, 42, 42L, 42.0f, 42.0d,
                new AtomicInteger(42), new AtomicLong(42), BigInteger.valueOf(42), new BigDecimal("42.0") };
        for (Number input : inputs) {
            assertEquals(expected, transformer.in(input), input.getClass().getSimpleName());
        }
    }

    static Stream<Arguments> smallIntegralTargets() {
        return Stream.of(
                Arguments.of("byte", -128L, 127L), Arguments.of(Byte.class.getName(), -128L, 127L),
                Arguments.of("short", -32768L, 32767L), Arguments.of(Short.class.getName(), -32768L, 32767L));
    }

    @ParameterizedTest
    @MethodSource("smallIntegralTargets")
    void byteAndShortConversionsAcceptInclusiveBounds(String target, long minimum, long maximum) {
        NumberTransformer transformer = new NumberTransformer(target);
        for (long bound : new long[] { minimum, maximum }) {
            Number[] inputs = { BigInteger.valueOf(bound), BigDecimal.valueOf(bound), bound, (int) bound,
                    (float) bound, (double) bound, new AtomicInteger((int) bound), new AtomicLong(bound) };
            for (Number input : inputs) {
                assertEquals(bound, transformer.in(input).longValue(), input.getClass().getSimpleName());
            }
        }
    }

    @ParameterizedTest
    @MethodSource("smallIntegralTargets")
    void byteAndShortConversionsRejectValuesImmediatelyOutsideTheirBounds(String target, long minimum, long maximum) {
        NumberTransformer transformer = new NumberTransformer(target);
        for (long overflow : new long[] { minimum - 1, maximum + 1 }) {
            Number[] inputs = { BigInteger.valueOf(overflow), BigDecimal.valueOf(overflow), overflow, (int) overflow,
                    (float) overflow, (double) overflow, new AtomicInteger((int) overflow), new AtomicLong(overflow) };
            for (Number input : inputs) {
                assertThrows(ArithmeticException.class, () -> transformer.in(input), input.getClass().getSimpleName());
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "byte", "java.lang.Byte", "short", "java.lang.Short" })
    void byteAndShortConversionsRejectFractionsAndNonFiniteValues(String target) {
        NumberTransformer transformer = new NumberTransformer(target);
        Number[] inputs = { new BigDecimal("1.5"), new BigDecimal("-1.5"), 1.5f, -1.5d,
                Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY,
                Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY };
        for (Number input : inputs) {
            assertThrows(ArithmeticException.class, () -> transformer.in(input), input.toString());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "float", "java.lang.Float" })
    void floatConversionsRejectFiniteOverflowAndNonFiniteInputs(String target) {
        NumberTransformer transformer = new NumberTransformer(target);
        Number[] inputs = { new BigDecimal("3.5e38"), new BigDecimal("-3.5e38"),
                BigInteger.ONE.shiftLeft(128), BigInteger.ONE.shiftLeft(128).negate(),
                Double.MAX_VALUE, -Double.MAX_VALUE, Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY,
                Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY };
        for (Number input : inputs) {
            assertThrows(ArithmeticException.class, () -> transformer.in(input), input.toString());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "float", "java.lang.Float" })
    void floatConversionsAllowRoundingSubnormalsAndUnderflow(String target) {
        NumberTransformer transformer = new NumberTransformer(target);
        assertEquals(0.1f, transformer.in(new BigDecimal("0.1")));
        assertEquals(16777216.0f, transformer.in(16777217));
        assertEquals(Float.MAX_VALUE, transformer.in(new BigDecimal("3.4028235e38")));
        assertEquals(Float.MIN_VALUE, transformer.in(new BigDecimal("1.4e-45")));
        assertEquals(0.0f, transformer.in(new BigDecimal("1e-1000")));
        assertEquals(-0.0f, transformer.in(new BigDecimal("-1e-1000")));
        assertEquals(-0.0f, transformer.in(-0.0d));
    }

    @ParameterizedTest
    @ValueSource(strings = { "double", "java.lang.Double" })
    void doubleConversionsRejectFiniteOverflowAndNonFiniteInputs(String target) {
        NumberTransformer transformer = new NumberTransformer(target);
        Number[] inputs = { new BigDecimal("1e309"), new BigDecimal("-1e309"),
                BigInteger.ONE.shiftLeft(1024), BigInteger.ONE.shiftLeft(1024).negate(),
                Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY,
                Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY };
        for (Number input : inputs) {
            assertThrows(ArithmeticException.class, () -> transformer.in(input), input.toString());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "double", "java.lang.Double" })
    void doubleConversionsAllowRoundingSubnormalsAndUnderflow(String target) {
        NumberTransformer transformer = new NumberTransformer(target);
        assertEquals(0.1d, transformer.in(new BigDecimal("0.1")));
        assertEquals(9007199254740992.0d, transformer.in(9007199254740993L));
        assertEquals(Double.MAX_VALUE, transformer.in(new BigDecimal("1.7976931348623157e308")));
        assertEquals(Double.MIN_VALUE, transformer.in(new BigDecimal("4.9e-324")));
        assertEquals(0.0d, transformer.in(new BigDecimal("1e-1000")));
        assertEquals(-0.0d, transformer.in(new BigDecimal("-1e-1000")));
        assertEquals(-0.0d, transformer.in(-0.0f));
    }

    @Test
    void bigIntegerConversionPreservesValuesBeyondLongRange() {
        NumberTransformer transformer = new NumberTransformer(BigInteger.class.getName());
        BigInteger large = new BigInteger("1267650600228229401496703205376");
        assertEquals(large, transformer.in(large));
        assertEquals(large, transformer.in(new BigDecimal("1267650600228229401496703205376.0")));
        assertEquals(large, transformer.in(0x1p100));
        assertEquals(large, transformer.in(0x1p100f));
        assertEquals(new BigInteger("-9223372036854775808"), transformer.in((double) Long.MIN_VALUE));
    }

    @Test
    void bigIntegerConversionRejectsFractionsAndNonFiniteInputs() {
        NumberTransformer transformer = new NumberTransformer(BigInteger.class.getName());
        Number[] inputs = { new BigDecimal("1.5"), new BigDecimal("-1.5"), 1.5f, -1.5d,
                Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY,
                Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY };
        for (Number input : inputs) {
            assertThrows(ArithmeticException.class, () -> transformer.in(input), input.toString());
        }
    }

    @Test
    void bigIntegerConversionRejectsAnExponentBeyondTheScalarAllocationGuard() {
        NumberTransformer transformer = new NumberTransformer(BigInteger.class.getName());
        assertThrows(ArithmeticException.class, () -> transformer.in(new BigDecimal("1e10001")));
    }

    @Test
    void bigDecimalConversionPreservesIntegralPrecisionAndDecimalScale() {
        NumberTransformer transformer = new NumberTransformer(BigDecimal.class.getName());
        assertEquals(new BigDecimal("9223372036854775807"), transformer.in(Long.MAX_VALUE));
        assertEquals(new BigDecimal("-9223372036854775808"), transformer.in(Long.MIN_VALUE));
        assertEquals(new BigDecimal("9223372036854775807"), transformer.in(new AtomicLong(Long.MAX_VALUE)));
        assertEquals(new BigDecimal("2147483647"), transformer.in(new AtomicInteger(Integer.MAX_VALUE)));
        assertEquals(new BigDecimal("1267650600228229401496703205376"),
                transformer.in(new BigInteger("1267650600228229401496703205376")));
        assertEquals(new BigDecimal("123456789012345678901234567890.1234500"),
                transformer.in(new BigDecimal("123456789012345678901234567890.1234500")));
    }

    @Test
    void bigDecimalConversionOfFloatUsesTheFloatsShortestDecimalNotItsWidenedDouble() {
        NumberTransformer transformer = new NumberTransformer(BigDecimal.class.getName());
        float fraction = 0.1f;
        assertEquals(new BigDecimal(Float.toString(fraction)), transformer.in(fraction));
        assertEquals(new BigDecimal(Float.MAX_VALUE), transformer.in(Float.MAX_VALUE));
        assertEquals(0, ((BigDecimal) transformer.in(Float.MAX_VALUE)).scale());
    }
}
