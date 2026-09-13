package io.smallrye.graphql.api;

import tools.jackson.databind.JsonNode;

/**
 * To add you own GraphQL error <code>extension</code> fields, you can add your own implementations
 * of this class via the {@link java.util.ServiceLoader ServiceLoader} mechanism.
 */
public interface ErrorExtensionProvider {
    String getKey();

    JsonNode mapValueFrom(Throwable exception);
}
