package io.smallrye.graphql.scalar.number;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.BigInteger;
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
import graphql.schema.CoercingSerializeException;

class NumberCoercingLiteralTest {

    static Stream<Arguments> floatingValues() {
        return Stream.of(
                Arguments.of(new BigDecimal("1.2"), 1.2d),
                Arguments.of(new BigDecimal("0.3"), 0.3d),
                Arguments.of(new BigDecimal("-1.2"), -1.2d),
                Arguments.of(new BigDecimal("0.0001"), 0.0001d),
                Arguments.of(new BigDecimal("9223372036854775808"), 0x1p63),
                Arguments.of(new BigInteger("18446744073709551616"), 0x1p64),
                Arguments.of(42L, 42.0d),
                Arguments.of(1.25f, 1.25d),
                Arguments.of(1.2d, 1.2d),
                Arguments.of("1.2", 1.2d));
    }

    @ParameterizedTest
    @MethodSource("floatingValues")
    void floatingLiteralsPreserveTheConvertedValue(Object input, double expected) {
        Coercing coercing = new FloatScalar().getCoercing();
        FloatValue literal = assertInstanceOf(FloatValue.class, coercing.valueToLiteral(input));
        assertEquals(expected, literal.getValue().doubleValue());
    }

    static Stream<Object> integralValues() {
        return Stream.of(new BigDecimal("42.0"), new BigInteger("42"), 42.0d, 42.0f, 42L, "42");
    }

    @ParameterizedTest
    @MethodSource("integralValues")
    void integerLiteralsUseTheConvertedIntegerType(Object input) {
        Coercing coercing = new IntegerScalar().getCoercing();
        IntValue literal = assertInstanceOf(IntValue.class, coercing.valueToLiteral(input));
        assertEquals(BigInteger.valueOf(42), literal.getValue());
    }

    @ParameterizedTest
    @ValueSource(strings = { "3.14", "-0.5", "0.0000000000000000000000001" })
    void fractionalIntegerLiteralsReportAnInvalidInteger(String input) {
        Coercing coercing = new IntegerScalar().getCoercing();
        for (Object literal : new Object[] { new StringValue(input), new FloatValue(new BigDecimal(input)),
                new BigDecimal(input) }) {
            CoercingParseLiteralException error = assertThrows(CoercingParseLiteralException.class,
                    () -> coercing.parseLiteral(literal));
            assertTrue(error.getMessage().contains("Can not parse a integer"), error.getMessage());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "2147483648", "-2147483649", "2147483647.5", "-2147483648.5" })
    void overflowingIntegerLiteralsRetainTheRangeDiagnostic(String input) {
        Coercing coercing = new IntegerScalar().getCoercing();
        for (Object literal : new Object[] { new StringValue(input), new FloatValue(new BigDecimal(input)),
                new BigDecimal(input) }) {
            CoercingParseLiteralException error = assertThrows(CoercingParseLiteralException.class,
                    () -> coercing.parseLiteral(literal));
            assertTrue(error.getMessage().contains("Int range"), error.getMessage());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "2147483648", "-2147483649", "3.14" })
    void invalidIntegerValuesCannotBypassValidationWhenCreatingALiteral(String input) {
        Coercing coercing = new IntegerScalar().getCoercing();
        assertThrows(CoercingSerializeException.class, () -> coercing.valueToLiteral(new BigDecimal(input)));
    }

    @Test
    void floatingOverflowWithAFractionStillReportsTheFloatingRange() {
        Coercing coercing = new FloatScalar().getCoercing();
        BigDecimal input = new BigDecimal("1e309").add(new BigDecimal("0.5"));
        CoercingParseLiteralException error = assertThrows(CoercingParseLiteralException.class,
                () -> coercing.parseLiteral(new FloatValue(input)));
        assertTrue(error.getMessage().contains("Float range"), error.getMessage());
    }
}
