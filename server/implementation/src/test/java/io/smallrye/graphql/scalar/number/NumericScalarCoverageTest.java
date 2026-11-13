package io.smallrye.graphql.scalar.number;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import graphql.language.FloatValue;
import graphql.language.IntValue;
import graphql.language.StringValue;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;

class NumericScalarCoverageTest {

    private final Coercing floating = new FloatScalar().getCoercing();

    /**
     * Widening a float to double exposes binary noise (0.1f becomes 0.10000000149011612), so a float result is kept
     * as is and the JSON writer prints its shortest decimal.
     */
    @Test
    void floatOutputIsReturnedUnchangedInsteadOfWidenedToDouble() {
        Float value = 0.1f;
        assertSame(value, floating.serialize(value));
    }

    @Test
    void floatToBigDecimalUsesTheFloatsShortestDecimal() {
        Coercing decimal = new BigDecimalScalar().getCoercing();
        float fraction = 0.1f;
        assertEquals(new BigDecimal(Float.toString(fraction)), decimal.serialize(fraction));
        BigDecimal integral = (BigDecimal) decimal.serialize(Float.MAX_VALUE);
        assertEquals(new BigDecimal(Float.MAX_VALUE), integral);
        assertEquals(0, integral.scale());
    }

    @Test
    void floatToIntegerScalarsRequiresAnExactIntegralValue() {
        Coercing integer = new IntegerScalar().getCoercing();
        Coercing bigInteger = new BigIntegerScalar().getCoercing();
        float integral = Short.MAX_VALUE;
        assertEquals((int) Short.MAX_VALUE, integer.serialize(integral));
        assertEquals(BigInteger.valueOf(Short.MAX_VALUE), bigInteger.serialize(integral));
        float fraction = 0.5f;
        assertThrows(CoercingSerializeException.class, () -> integer.serialize(fraction));
        assertThrows(CoercingSerializeException.class, () -> bigInteger.serialize(fraction));
    }

    static Stream<Object> invalidFloatingValues() {
        return Stream.of(Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY,
                Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY,
                new BigDecimal("1e309"), new BigDecimal("-1e309"),
                BigInteger.ONE.shiftLeft(1024), BigInteger.ONE.shiftLeft(1024).negate(),
                "NaN", "Infinity", "-Infinity", "1e309", "-1e309");
    }

    @ParameterizedTest
    @MethodSource("invalidFloatingValues")
    void floatingVariablesRejectNonFiniteValuesAndFiniteOverflow(Object input) {
        assertThrows(CoercingParseValueException.class, () -> floating.parseValue(input));
    }

    @ParameterizedTest
    @MethodSource("invalidFloatingValues")
    void floatingOutputRejectsNonFiniteValuesAndFiniteOverflow(Object input) {
        assertThrows(CoercingSerializeException.class, () -> floating.serialize(input));
    }

    @ParameterizedTest
    @ValueSource(strings = { "1e309", "-1e309" })
    void floatingDecimalAndStringLiteralsRejectFiniteOverflow(String input) {
        assertThrows(CoercingParseLiteralException.class,
                () -> floating.parseLiteral(new FloatValue(new BigDecimal(input))));
        assertThrows(CoercingParseLiteralException.class,
                () -> floating.parseLiteral(new StringValue(input)));
    }

    @ParameterizedTest
    @ValueSource(strings = { "NaN", "Infinity", "-Infinity" })
    void floatingStringLiteralsRejectNonFiniteValues(String input) {
        assertThrows(CoercingParseLiteralException.class, () -> floating.parseLiteral(new StringValue(input)));
    }

    @Test
    void floatingIntegerLiteralsRejectFiniteOverflow() {
        BigInteger overflow = BigInteger.ONE.shiftLeft(1024);
        assertThrows(CoercingParseLiteralException.class, () -> floating.parseLiteral(new IntValue(overflow)));
        assertThrows(CoercingParseLiteralException.class, () -> floating.parseLiteral(new IntValue(overflow.negate())));
    }

    static Stream<Arguments> validFloatingValues() {
        return Stream.of(
                Arguments.of((byte) 42, 42.0d), Arguments.of((short) 42, 42.0d),
                Arguments.of(42, 42.0d), Arguments.of(42L, 42.0d),
                Arguments.of(new AtomicInteger(42), 42.0d), Arguments.of(new AtomicLong(42), 42.0d),
                Arguments.of(new BigInteger("42"), 42.0d), Arguments.of(new BigDecimal("1.25"), 1.25d),
                Arguments.of(1.25f, 1.25d), Arguments.of(1.25d, 1.25d),
                Arguments.of(new BigDecimal("0.1"), 0.1d),
                Arguments.of(new BigDecimal("4.9e-324"), Double.MIN_VALUE),
                Arguments.of(new BigDecimal("1e-1000"), 0.0d),
                Arguments.of(new BigDecimal("-1e-1000"), -0.0d),
                Arguments.of(-0.0f, -0.0d), Arguments.of(-0.0d, -0.0d));
    }

    @ParameterizedTest
    @MethodSource("validFloatingValues")
    void floatingVariablesAndOutputAllowFiniteRoundingSubnormalsAndUnderflow(Object input, double expected) {
        assertEquals(expected, ((Number) floating.parseValue(input)).doubleValue());
        assertEquals(expected, ((Number) floating.serialize(input)).doubleValue());
    }

    @ParameterizedTest
    @ValueSource(strings = { "1.25", "4.9e-324", "1e-1000" })
    void finiteFloatingLiteralsRemainAccepted(String input) {
        double expected = switch (input) {
            case "1.25" -> 1.25d;
            case "4.9e-324" -> Double.MIN_VALUE;
            default -> 0.0d;
        };
        assertEquals(expected, ((Number) floating.parseLiteral(new FloatValue(new BigDecimal(input)))).doubleValue());
        assertEquals(expected, ((Number) floating.parseLiteral(new StringValue(input))).doubleValue());
    }

    static Stream<Arguments> supportedScalarWrappers() {
        Coercing integer = new IntegerScalar().getCoercing();
        Coercing bigInteger = new BigIntegerScalar().getCoercing();
        Coercing floating = new FloatScalar().getCoercing();
        return Stream.of(
                Arguments.of(integer, new AtomicInteger(Integer.MIN_VALUE)),
                Arguments.of(integer, new AtomicInteger(Integer.MAX_VALUE)),
                Arguments.of(integer, OptionalInt.of(Integer.MIN_VALUE)),
                Arguments.of(integer, OptionalInt.of(Integer.MAX_VALUE)),
                Arguments.of(bigInteger, new AtomicLong(Long.MIN_VALUE)),
                Arguments.of(bigInteger, new AtomicLong(Long.MAX_VALUE)),
                Arguments.of(bigInteger, OptionalLong.of(Long.MIN_VALUE)),
                Arguments.of(bigInteger, OptionalLong.of(Long.MAX_VALUE)),
                Arguments.of(floating, OptionalDouble.of(Double.MIN_VALUE)),
                Arguments.of(floating, OptionalDouble.of(Double.MAX_VALUE)));
    }

    @ParameterizedTest
    @MethodSource("supportedScalarWrappers")
    void supportedAtomicAndOptionalWrappersRetainTheirExistingScalarForm(Coercing coercing, Object wrapper) {
        assertSame(wrapper, coercing.parseValue(wrapper));
        assertSame(wrapper, coercing.serialize(wrapper));
    }

    @Test
    void atomicLongInputsToIntCannotBypassTheIntegerRangeCheck() {
        Coercing integer = new IntegerScalar().getCoercing();
        for (long value : new long[] { -2147483649L, 2147483648L }) {
            assertThrows(CoercingParseValueException.class, () -> integer.parseValue(new AtomicLong(value)));
            assertThrows(CoercingSerializeException.class, () -> integer.serialize(new AtomicLong(value)));
        }
    }

    @ParameterizedTest
    @ValueSource(doubles = { Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY })
    void optionalDoubleCannotBypassFiniteValidation(double value) {
        OptionalDouble optional = OptionalDouble.of(value);
        assertThrows(CoercingParseValueException.class, () -> floating.parseValue(optional));
        assertThrows(CoercingSerializeException.class, () -> floating.serialize(optional));
    }

    @Test
    void bigIntegerScalarDoesNotApplyALongRangeLimitToActualBigIntegerValues() {
        Coercing bigInteger = new BigIntegerScalar().getCoercing();
        BigInteger value = new BigInteger("1267650600228229401496703205376");
        assertEquals(value, bigInteger.parseValue(value));
        assertEquals(value, bigInteger.serialize(value));
        assertEquals(value, bigInteger.parseLiteral(new IntValue(value)));
    }

    @Test
    void bigDecimalScalarPreservesLargeIntegerPrecisionAndDecimalScale() {
        Coercing decimal = new BigDecimalScalar().getCoercing();
        BigInteger integer = new BigInteger("1267650600228229401496703205376");
        BigDecimal expected = new BigDecimal("1267650600228229401496703205376");
        assertEquals(expected, decimal.parseValue(integer));
        assertEquals(expected, decimal.serialize(integer));
        assertEquals(expected, decimal.parseLiteral(new IntValue(integer)));
        BigDecimal scaled = new BigDecimal("123456789012345678901234567890.1234500");
        assertEquals(scaled, decimal.parseValue(scaled));
        assertEquals(scaled, decimal.serialize(scaled));
        assertEquals(scaled, decimal.parseLiteral(new FloatValue(scaled)));
    }
}
