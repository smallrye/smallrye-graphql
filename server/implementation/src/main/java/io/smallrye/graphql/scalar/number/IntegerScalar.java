package io.smallrye.graphql.scalar.number;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.OptionalInt;
import java.util.concurrent.atomic.AtomicInteger;

import graphql.Scalars;

/**
 * Scalar for Integer.
 * Based on graphql-java's Scalars.GraphQLInt
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class IntegerScalar extends AbstractNumberScalar {

    public IntegerScalar() {

        super(Scalars.GraphQLInt.getName(),
                new Converter() {
                    @Override
                    public Object fromBigDecimal(BigDecimal bigDecimal) {
                        return bigDecimal.intValueExact();
                    }

                    @Override
                    public Object fromBigInteger(BigInteger bigInteger) {
                        return bigInteger.intValue();
                    }

                    @Override
                    public boolean isInRange(BigInteger value) {
                        return (value.compareTo(BigInteger.valueOf(Integer.MIN_VALUE)) < 0
                                || value.compareTo(BigInteger.valueOf(Integer.MIN_VALUE)) > 0);
                    }

                },
                AtomicInteger.class, OptionalInt.class, Integer.class, int.class, Short.class, short.class, Byte.class,
                byte.class);
    }
}
