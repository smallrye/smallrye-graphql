package io.smallrye.graphql.api;

import java.math.BigInteger;

/**
 * A base class for all CustomScalars that are based on GraphQL's Int.
 */
public interface CustomIntScalar {
    BigInteger intValueForSerialization();
}
