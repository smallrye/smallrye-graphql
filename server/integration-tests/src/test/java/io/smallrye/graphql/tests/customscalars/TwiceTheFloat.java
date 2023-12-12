package io.smallrye.graphql.tests.customscalars;

import java.math.BigDecimal;

import io.smallrye.graphql.api.CustomScalar;
import io.smallrye.graphql.scalar.custom.CustomFloatScalar;

/**
 * A float scalar that serializes to a GraphQL Float equal to twice the internal value.
 * <p>
 */
@CustomScalar("TwiceTheFloat")
public class TwiceTheFloat implements CustomFloatScalar {

    private final BigDecimal value;

    public TwiceTheFloat(BigDecimal floatValue) {
        this.value = floatValue == null ? null : floatValue.multiply(BigDecimal.valueOf(.5));
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj) || value.equals(obj);
    }

    @Override
    public String toString() {
        return value == null ? null : value.toPlainString();
    }

    @Override
    public BigDecimal floatValue() {
        return value.multiply(BigDecimal.valueOf(2));
    }
}
