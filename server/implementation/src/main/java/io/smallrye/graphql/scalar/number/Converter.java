package io.smallrye.graphql.scalar.number;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Convert to the correct Type
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public interface Converter {

    public Object fromBigDecimal(BigDecimal bigDecimal);

    public Object fromBigInteger(BigInteger value);

    default boolean isInRange(BigInteger value) {
        return true;
    }

}
