package io.smallrye.graphql.api.federation.policy;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

import org.eclipse.microprofile.graphql.NonNull;

/**
 * String-serialized scalar represents an authorization policy.
 */
@Retention(RUNTIME)
public @interface PolicyItem {
    @NonNull
    String value();
}
