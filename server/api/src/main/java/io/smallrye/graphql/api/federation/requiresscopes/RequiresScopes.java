package io.smallrye.graphql.api.federation.requiresscopes;

import static io.smallrye.graphql.api.DirectiveLocation.ENUM;
import static io.smallrye.graphql.api.DirectiveLocation.FIELD_DEFINITION;
import static io.smallrye.graphql.api.DirectiveLocation.INTERFACE;
import static io.smallrye.graphql.api.DirectiveLocation.OBJECT;
import static io.smallrye.graphql.api.DirectiveLocation.SCALAR;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.NonNull;

import io.smallrye.common.annotation.Experimental;
import io.smallrye.graphql.api.Directive;

/**
 * <b><code>directive @requiresScopes(scopes: [[Scope!]!]!) on ENUM | FIELD_DEFINITION | INTERFACE | OBJECT |
 * SCALAR</code></b>
 *
 * @see <a href="https://www.apollographql.com/docs/federation/federated-types/federated-directives/#requiresscopes">
 *      federation spec</a>
 */
@Directive(on = { ENUM, FIELD_DEFINITION, INTERFACE, OBJECT, SCALAR })
@Description("Indicates to composition that the target element is accessible only to the authenticated supergraph " +
        "users with the appropriate JWT scopes")
@Retention(RUNTIME)
@Experimental("SmallRye GraphQL Federation is still subject to change.")
public @interface RequiresScopes {
    @NonNull
    @Description("List of JWT scopes that must be granted to the user in order to access the underlying element data.")
    ScopeGroup[] scopes();
}
