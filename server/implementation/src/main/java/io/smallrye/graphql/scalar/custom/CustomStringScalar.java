package io.smallrye.graphql.scalar.custom;

/**
 * A base class for all CustomScalars that are based on GraphQL's String.
 */
public interface CustomStringScalar {
    String stringValueForSerialization();
}
