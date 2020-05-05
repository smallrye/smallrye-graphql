package io.smallrye.graphql.scalar.number;

import java.math.BigDecimal;
import java.math.BigInteger;

import graphql.Scalars;
import io.smallrye.graphql.execution.Classes;
import io.smallrye.graphql.schema.model.Argument;

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
                    public Object fromNumber(Number number, Argument argument) {
                        String argumentName = argument.getReference().getClassName();

                        if (argumentName.equals(Classes.SHORT) || argumentName.equals(Classes.SHORT_PRIMATIVE)) {
                            return number.shortValue();
                        } else if (argumentName.equals(Classes.BYTE) || argumentName.equals(Classes.BYTE_PRIMATIVE)) {
                            return number.byteValue();
                        } else if (argumentName.equals(Classes.INTEGER) || argumentName.equals(Classes.INTEGER_PRIMATIVE)) {
                            return number.intValue();
                        }
                        return number;
                    }

                    @Override
                    public boolean isInRange(BigInteger value) {
                        return (value.compareTo(BigInteger.valueOf(Integer.MIN_VALUE)) < 0
                                || value.compareTo(BigInteger.valueOf(Integer.MIN_VALUE)) > 0);
                    }

                },
                Integer.class, int.class, Short.class, short.class, Byte.class, byte.class);
    }
}