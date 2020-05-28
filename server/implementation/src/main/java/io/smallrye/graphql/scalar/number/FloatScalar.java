package io.smallrye.graphql.scalar.number;

import java.math.BigDecimal;
import java.math.BigInteger;

import graphql.Scalars;

/**
 * Scalar for Float.
 * Based on graphql-java's Scalars.GraphQLFloat
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class FloatScalar extends AbstractNumberScalar {

    public FloatScalar() {

        super(Scalars.GraphQLFloat.getName(),
                new Converter() {
                    @Override
                    public Object fromBigDecimal(BigDecimal bigDecimal) {
                        return bigDecimal.floatValue();
                    }

                    @Override
                    public Object fromBigInteger(BigInteger bigInteger) {
                        return bigInteger.floatValue();
                    }

                },
                Float.class, float.class, Double.class, double.class);
    }
}
