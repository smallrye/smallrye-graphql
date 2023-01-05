package io.smallrye.graphql.api.federation;

import static io.smallrye.graphql.api.DirectiveLocation.INTERFACE;
import static io.smallrye.graphql.api.DirectiveLocation.OBJECT;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

import org.eclipse.microprofile.graphql.Description;

import io.smallrye.common.annotation.Experimental;
import io.smallrye.graphql.api.Directive;

/**
 * <b><code>directive @extends on OBJECT | INTERFACE</code></b>
 *
 * @see <a href="https://www.apollographql.com/docs/federation/federated-types/federated-directives/#extends">federation spec</a>
 */
@Directive(on = { OBJECT, INTERFACE })
@Description("Indicates that an object or interface definition is an extension of another definition of that same type.\n" +
             "If your subgraph library supports GraphQL's built-in extend keyword, do not use this directive! Instead, use extend.")
@Retention(RUNTIME)
@Experimental("SmallRye GraphQL Federation is still subject to change. " +
        "Additionally, this annotation is currently only a directive without explicit support from the extension.")
public @interface Extends {
}
