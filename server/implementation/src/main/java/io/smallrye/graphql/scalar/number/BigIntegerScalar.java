package io.smallrye.graphql.scalar.number;

import java.math.BigDecimal;
import java.math.BigInteger;

import graphql.Scalars;
import io.smallrye.graphql.execution.Classes;
import io.smallrye.graphql.schema.model.Argument;

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

                    @Override
                    public Object fromNumber(Number number, Argument argument) {
                        String argumentName = argument.getReference().getClassName();

                        if (argumentName.equals(Classes.LONG) || argumentName.equals(Classes.LONG_PRIMATIVE)) {
                            return number.longValue();
                        } else {
                            return new BigInteger(number.toString());
                        }
                    }
                },
                BigInteger.class, Long.class, long.class);
    }
}