package io.smallrye.graphql.scalar.number;

import java.math.BigDecimal;
import java.math.BigInteger;

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
                },
                BigDecimal.class);
    }

}
