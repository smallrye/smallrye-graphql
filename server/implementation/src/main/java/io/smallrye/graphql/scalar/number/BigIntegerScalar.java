package io.smallrye.graphql.scalar.number;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.OptionalLong;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Scalar for BigInteger.
 * Based on graphql-java's Scalars.GraphQLBigInteger
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class BigIntegerScalar extends AbstractNumberScalar {

    /**
     * Upper bounds guarding against unbounded {@link BigInteger} allocation (CWE-770). A compact literal such as
     * {@code 1e64000000} would otherwise expand, via {@link BigDecimal#toBigIntegerExact()}, into a BigInteger with tens of
     * millions of digits, causing CPU exhaustion or {@link OutOfMemoryError} during literal parsing (before authorization).
     * These limits are far larger than any real {@code Long} or practical {@code BigInteger} value (e.g. an RSA-4096 modulus
     * is ~1234 digits) while keeping the exact conversion cheap.
     */
    private static final int MAX_PRECISION = 10_000;
    private static final int MAX_SCALE = 10_000;

    public BigIntegerScalar() {

        super("BigInteger",
                new Converter() {
                    @Override
                    public Object fromBigDecimal(BigDecimal bigDecimal) {
                        // Reject implausibly large magnitudes before the (potentially huge) exact conversion. Both a large
                        // negative scale (huge integer part) and a large positive scale (division by 10^scale) are expensive,
                        // so bound the absolute scale as well as the precision. Thrown as an ArithmeticException so that
                        // NumberCoercing.parseLiteral translates it into a normal CoercingParseLiteralException instead of
                        // exhausting resources.
                        if (bigDecimal.precision() > MAX_PRECISION || Math.abs((long) bigDecimal.scale()) > MAX_SCALE) {
                            throw new ArithmeticException("Numeric literal magnitude out of allowed range");
                        }
                        return bigDecimal.toBigIntegerExact();
                    }

                    @Override
                    public Object fromBigInteger(BigInteger bigInteger) {
                        return bigInteger;
                    }

                },
                AtomicLong.class, OptionalLong.class, BigInteger.class, Long.class, long.class);
    }
}
