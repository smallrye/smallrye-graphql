package io.smallrye.graphql.api;

import javax.json.JsonValue;

/**
 * To add you own GraphQL error <code>extension</code> fields, you can add your own implementations
 * of this class via the {@link java.util.ServiceLoader ServiceLoader} mechanism.
 */
public interface ErrorExtensionProvider {
    String getKey();

    JsonValue mapValueFrom(Throwable exception);
}
