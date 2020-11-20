package io.smallrye.graphql.client.typesafe.api;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

/**
 * Allow this field only when the containing class is used as a <code>Type</code>s, but not
 * when used as an <code>Input</code>. This happens when the GraphQL service has resolver
 * methods (see the <code>@Source</code> annotation).
 */
@Retention(RUNTIME)
public @interface OutputOnly {
}
