package io.smallrye.graphql.scalar.custom;

import java.math.BigDecimal;

/**
 * A base class for all CustomScalars that are based on GraphQL's Float.
 */
public interface CustomFloatScalar {
    BigDecimal floatValueForSerialization();
}
