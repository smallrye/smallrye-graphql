package io.smallrye.graphql.scalar.number;

import java.math.BigDecimal;
import java.math.BigInteger;

import io.smallrye.graphql.transformation.NumberTransformer;

/**
 * Convert to the correct Type
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public interface Converter {

    public Object fromBigDecimal(BigDecimal bigDecimal);

    public Object fromBigInteger(BigInteger value);

    default Object fromLong(long value) {
        return fromBigInteger(BigInteger.valueOf(value));
    }

    default Object fromFloat(float value) {
        // Widening a float to double is exact
        return fromDouble(value);
    }

    default Object fromDouble(double value) {
        return fromBigDecimal(NumberTransformer.bigDecimalValue(value));
    }

}
