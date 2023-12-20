package io.smallrye.graphql.api;

import java.math.BigDecimal;

/**
 * A base class for all CustomScalars that are based on GraphQL's Float.
 */
public interface CustomFloatScalar {
    BigDecimal floatValueForSerialization();
}
