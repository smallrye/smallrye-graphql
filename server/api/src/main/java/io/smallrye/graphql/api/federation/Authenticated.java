package io.smallrye.graphql.api.federation;

import static io.smallrye.graphql.api.DirectiveLocation.ENUM;
import static io.smallrye.graphql.api.DirectiveLocation.FIELD_DEFINITION;
import static io.smallrye.graphql.api.DirectiveLocation.INTERFACE;
import static io.smallrye.graphql.api.DirectiveLocation.OBJECT;
import static io.smallrye.graphql.api.DirectiveLocation.SCALAR;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

import org.eclipse.microprofile.graphql.Description;

import io.smallrye.common.annotation.Experimental;
import io.smallrye.graphql.api.Directive;

/**
 * <b><code>directive @authenticated on ENUM | FIELD_DEFINITION | INTERFACE | OBJECT | SCALAR</code></b>
 *
 * @see <a href="https://www.apollographql.com/docs/federation/federated-types/federated-directives/#authenticated">federation
 *      spec</a>
 */
@Directive(on = { ENUM, FIELD_DEFINITION, INTERFACE, OBJECT, SCALAR })
@Description("Indicates to composition that the target element is accessible only to the authenticated supergraph " +
        "users.")
@Retention(RUNTIME)
@Experimental("SmallRye GraphQL Federation is still subject to change.")
public @interface Authenticated {
}
