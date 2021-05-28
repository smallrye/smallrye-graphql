package io.smallrye.graphql.federation.api;

import static io.smallrye.graphql.api.DirectiveLocation.INTERFACE;
import static io.smallrye.graphql.api.DirectiveLocation.OBJECT;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

import io.smallrye.common.annotation.Experimental;
import org.eclipse.microprofile.graphql.Description;

import io.smallrye.graphql.api.Directive;

/** <b><code>directive @extends on OBJECT | INTERFACE</code></b> */
@Directive(on = { OBJECT, INTERFACE })
@Description("Some libraries such as graphql-java don't have native support for type extensions in their printer. " +
        "Apollo Federation supports using an @extends directive in place of extend type to annotate type references.")
@Retention(RUNTIME)
@Experimental("SmallRye GraphQL Federation is still subject to change. " +
    "Additionally, this annotation is currently only a directive without explicit support from the extension.")
public @interface Extends {
}
