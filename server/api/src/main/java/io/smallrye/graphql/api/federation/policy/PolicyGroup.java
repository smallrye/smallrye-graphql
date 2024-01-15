package io.smallrye.graphql.api.federation.policy;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

import org.eclipse.microprofile.graphql.NonNull;

/**
 * Defines a group of authorization policies, each representing a set used by the {@link Policy} directive.
 */
@Retention(RUNTIME)
public @interface PolicyGroup {
    @NonNull
    String[] value();
}
