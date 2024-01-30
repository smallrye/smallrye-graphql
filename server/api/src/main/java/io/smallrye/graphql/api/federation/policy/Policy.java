package io.smallrye.graphql.api.federation.policy;

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
 * <b><code>directive @policy(policies: [[Policy!]!]!) on FIELD_DEFINITION | OBJECT | INTERFACE | SCALAR |
 * ENUM</code></b>
 *
 * @see <a href="https://www.apollographql.com/docs/federation/federated-types/federated-directives/#policy">
 *      federation spec</a>
 */
@Directive(on = { FIELD_DEFINITION, OBJECT, INTERFACE, SCALAR, ENUM })
@Description("Indicates to composition that the target element is restricted based on authorization policies that are" +
        " evaluated in a Rhai script or coprocessor.")
@Retention(RUNTIME)
@Experimental("SmallRye GraphQL Federation is still subject to change.")
public @interface Policy {
    @NonNull
    @Description("List of authorization policies to evaluate.")
    PolicyGroup[] policies();
}
