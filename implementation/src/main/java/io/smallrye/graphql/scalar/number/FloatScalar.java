package io.smallrye.graphql.scalar.number;

import java.math.BigDecimal;
import java.math.BigInteger;

import graphql.Scalars;
import io.smallrye.graphql.execution.Classes;
import io.smallrye.graphql.schema.model.Argument;

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

                    @Override
                    public Object fromNumber(Number number, Argument argument) {
                        String argumentName = argument.getReference().getClassName();

                        if (argumentName.equals(Classes.FLOAT) || argumentName.equals(Classes.FLOAT_PRIMATIVE)) {
                            return number.floatValue();
                        } else {
                            return number.doubleValue();
                        }
                    }
                },
                Float.class, float.class, Double.class, double.class);
    }
}