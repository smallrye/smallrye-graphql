package io.smallrye.graphql.scalar.number;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.OptionalInt;
import java.util.concurrent.atomic.AtomicInteger;

import graphql.Scalars;
import io.smallrye.graphql.transformation.NumberTransformer;

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
                        return bigInteger.intValueExact();
                    }

                    @Override
                    public Object fromLong(long value) {
                        return Math.toIntExact(value);
                    }

                    @Override
                    public Object fromDouble(double value) {
                        return NumberTransformer.intValueExact(value);
                    }

                },
                AtomicInteger.class, OptionalInt.class, Integer.class, int.class, Short.class, short.class, Byte.class,
                byte.class);
    }
}
