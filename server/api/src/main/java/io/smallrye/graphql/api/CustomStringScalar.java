package io.smallrye.graphql.api;

/**
 * A base class for all CustomScalars that are based on GraphQL's String.
 */
public interface CustomStringScalar {
    String stringValueForSerialization();
}
