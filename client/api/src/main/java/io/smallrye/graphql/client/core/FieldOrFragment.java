package io.smallrye.graphql.client.core;

/**
 * Represents one of these nodes in a GraphQL document:
 * - regular field (name), for example "color"
 * - reference to a named fragment, for example "...comparisonFields"
 * - an inline fragment, for example ("... on Person { name } ")
 */
public interface FieldOrFragment extends Buildable {
}
