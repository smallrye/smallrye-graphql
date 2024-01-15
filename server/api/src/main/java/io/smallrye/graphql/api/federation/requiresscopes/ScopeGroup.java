package io.smallrye.graphql.api.federation.requiresscopes;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

import org.eclipse.microprofile.graphql.NonNull;

/**
 * Defines a group of JWT scopes, each representing a set used by the {@link RequiresScopes} directive.
 */
@Retention(RUNTIME)
public @interface ScopeGroup {
    @NonNull
    String[] value();
}
