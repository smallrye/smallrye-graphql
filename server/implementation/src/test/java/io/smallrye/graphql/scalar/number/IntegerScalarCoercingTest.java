package io.smallrye.graphql.scalar.number;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import graphql.language.FloatValue;
import graphql.language.IntValue;
import graphql.language.StringValue;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;

class IntegerScalarCoercingTest {

    private final Coercing coercing = new IntegerScalar().getCoercing();

    @ParameterizedTest
    @ValueSource(ints = { Integer.MIN_VALUE, 0, Integer.MAX_VALUE })
    void acceptsInclusiveLiteralBounds(int value) {
        assertEquals(value, coercing.parseLiteral(new IntValue(BigInteger.valueOf(value))));
    }

    @ParameterizedTest
    @ValueSource(strings = { "-2147483649", "2147483648", "-99999999999999999999999999999",
            "99999999999999999999999999999" })
    void rejectsOverflowingIntegerLiterals(String value) {
        assertThrows(CoercingParseLiteralException.class,
                () -> coercing.parseLiteral(new IntValue(new BigInteger(value))));
        assertThrows(CoercingParseLiteralException.class,
                () -> coercing.parseLiteral(new BigInteger(value)));
    }

    static Stream<Object> validVariables() {
        return Stream.of(new BigInteger("2147483647"), new BigDecimal("2147483647.0"), 2147483647L,
                "2147483647", new BigInteger("-2147483648"), new BigDecimal("-2147483648.0"));
    }

    @ParameterizedTest
    @MethodSource("validVariables")
    void normalizesVariablesThroughTheConverter(Object value) {
        int expected = value.toString().startsWith("-") ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        assertEquals(expected, coercing.parseValue(value));
    }

    static Stream<Object> invalidVariables() {
        return Stream.of(new BigInteger("2147483648"), new BigInteger("-2147483649"),
                new BigDecimal("2147483648"), new BigDecimal("-2147483649"),
                new BigDecimal("1.5"), 2147483648L, -2147483649L, 1.5d,
                "99999999999999999999999999999", "-99999999999999999999999999999", "1.5");
    }

    @ParameterizedTest
    @MethodSource("invalidVariables")
    void rejectsInvalidVariablesWithACoercingException(Object value) {
        assertThrows(CoercingParseValueException.class, () -> coercing.parseValue(value));
    }

    @ParameterizedTest
    @MethodSource("invalidVariables")
    void rejectsInvalidOutputWithASerializeException(Object value) {
        assertThrows(CoercingSerializeException.class, () -> coercing.serialize(value));
    }

    @ParameterizedTest
    @ValueSource(strings = { "2147483648", "-2147483649", "1.5" })
    void rejectsInvalidDecimalAndStringLiterals(String value) {
        assertThrows(CoercingParseLiteralException.class,
                () -> coercing.parseLiteral(new FloatValue(new BigDecimal(value))));
        assertThrows(CoercingParseLiteralException.class,
                () -> coercing.parseLiteral(new StringValue(value)));
    }
}
