package io.smallrye.graphql.tests.customscalars;

import java.math.BigDecimal;

import io.smallrye.graphql.api.CustomScalar;
import io.smallrye.graphql.scalar.custom.CustomStringScalar;

/**
 * An alternative BigDecimal scalar that serializes to a GraphQL String instead of a Float.
 * <p>
 */
@CustomScalar("BigDecimalString")
public class BigDecimalString implements CustomStringScalar {

    private final BigDecimal value;

    public BigDecimalString(String stringValue) {
        this.value = stringValue == null ? null : new BigDecimal(stringValue);
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
    public String stringValue() {
        return toString();
    }
}
