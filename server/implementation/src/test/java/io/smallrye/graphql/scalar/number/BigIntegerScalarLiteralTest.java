package io.smallrye.graphql.scalar.number;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;

import org.junit.jupiter.api.Test;

import graphql.language.FloatValue;
import graphql.language.IntValue;
import graphql.language.StringValue;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;

/**
 * Guards against unbounded {@link BigInteger} allocation (CWE-770) when parsing numeric literals mapped to the
 * {@code BigInteger} scalar (which also backs {@code Long}, {@code AtomicLong} and {@code OptionalLong}). A compact literal
 * such as {@code 1e64000000} must be rejected quickly instead of expanding into a BigInteger with tens of millions of digits.
 */
class BigIntegerScalarLiteralTest {

    private final Coercing coercing = new BigIntegerScalar().getCoercing();

    @Test
    void hugeExponentFloatLiteralIsRejectedQuickly() {
        // Would otherwise allocate a ~64 million digit BigInteger and hang.
        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> assertThrows(CoercingParseLiteralException.class,
                () -> coercing.parseLiteral(new FloatValue(new BigDecimal("1e64000000")))));
    }

    @Test
    void hugeExponentStringLiteralIsRejectedQuickly() {
        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> assertThrows(CoercingParseLiteralException.class,
                () -> coercing.parseLiteral(new StringValue("1e64000000"))));
    }

    @Test
    void hugeNegativeExponentLiteralIsRejectedQuickly() {
        // A large positive scale forces a division by 10^scale, which is equally expensive.
        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> assertThrows(CoercingParseLiteralException.class,
                () -> coercing.parseLiteral(new FloatValue(new BigDecimal("1e-64000000")))));
    }

    @Test
    void ordinaryFloatLiteralStillParses() {
        assertEquals(BigInteger.valueOf(100), coercing.parseLiteral(new FloatValue(new BigDecimal("1e2"))));
    }

    @Test
    void ordinaryIntLiteralStillParses() {
        assertEquals(BigInteger.valueOf(42), coercing.parseLiteral(new IntValue(BigInteger.valueOf(42))));
    }

    @Test
    void largeButLegitimateIntegerStillParses() {
        // Well within the allowed bounds (e.g. an RSA-4096 modulus is ~1234 digits); must not be rejected.
        BigInteger big = BigInteger.TEN.pow(500);
        assertEquals(big, coercing.parseLiteral(new FloatValue(new BigDecimal(big))));
    }
}
