package io.smallrye.graphql.scalar.number;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.OptionalLong;
import java.util.concurrent.atomic.AtomicLong;

import graphql.Scalars;

/**
 * Scalar for BigInteger.
 * Based on graphql-java's Scalars.GraphQLBigInteger
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class BigIntegerScalar extends AbstractNumberScalar {

    public BigIntegerScalar() {

        super(Scalars.GraphQLBigInteger.getName(),
                new Converter() {
                    @Override
                    public Object fromBigDecimal(BigDecimal bigDecimal) {
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
