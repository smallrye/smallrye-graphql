package io.smallrye.graphql.api.federation.requiresscopes;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

import org.eclipse.microprofile.graphql.NonNull;

/**
 * String-serialized scalar represents a JWT scope.
 */
@Retention(RUNTIME)
public @interface ScopeItem {
    @NonNull
    String value();
}
