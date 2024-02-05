package io.smallrye.graphql.api.federation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

import org.eclipse.microprofile.graphql.NonNull;

/**
 * String-serialized scalar represents a set of fields that's passed to a federated directive.
 */
@Retention(RUNTIME)
public @interface FieldSet {
    @NonNull
    String value();
}
