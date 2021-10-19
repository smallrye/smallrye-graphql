package io.smallrye.graphql.client.core;

/**
 * Represents a node in a GraphQL document that can contain either an operation (query/mutation/subscription),
 * or a definition of a named fragment. On the top level, a GraphQL document basically consists of a list
 * of these nodes.
 */
public interface FragmentOrOperation extends Buildable {
}
