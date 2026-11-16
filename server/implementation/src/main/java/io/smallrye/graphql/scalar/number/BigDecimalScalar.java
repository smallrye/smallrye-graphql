package io.smallrye.graphql.scalar.number;

import java.math.BigDecimal;
import java.math.BigInteger;

import io.smallrye.graphql.transformation.NumberTransformer;

/**
 * Scalar for BigDecimal.
 * Based on graphql-java's Scalars.GraphQLBigDecimal
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class BigDecimalScalar extends AbstractNumberScalar {

    public BigDecimalScalar() {

        super("BigDecimal",
                new Converter() {
                    @Override
                    public Object fromBigDecimal(BigDecimal bigDecimal) {
                        return bigDecimal;
                    }

                    @Override
                    public Object fromBigInteger(BigInteger bigInteger) {
                        return new BigDecimal(bigInteger);
                    }

                    @Override
                    public Object fromLong(long value) {
                        return BigDecimal.valueOf(value);
                    }

                    @Override
                    public Object fromFloat(float value) {
                        return NumberTransformer.bigDecimalValue(value);
                    }
                },
                BigDecimal.class);
    }

}
