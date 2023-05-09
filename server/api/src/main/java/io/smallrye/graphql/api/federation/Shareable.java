package io.smallrye.graphql.api.federation;

import static io.smallrye.graphql.api.DirectiveLocation.FIELD_DEFINITION;
import static io.smallrye.graphql.api.DirectiveLocation.OBJECT;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

import org.eclipse.microprofile.graphql.Description;

import io.smallrye.common.annotation.Experimental;
import io.smallrye.graphql.api.Directive;

/**
 * <b><code>directive @shareable on FIELD_DEFINITION | OBJECT</code></b>
 *
 * @see <a href="https://www.apollographql.com/docs/federation/federated-types/federated-directives/#shareable">federation
 *      spec</a>
 */
@Directive(on = { FIELD_DEFINITION, OBJECT })
@Description("Indicates that an object type's field is allowed to be resolved by multiple subgraphs" +
        " (by default in Federation 2, object fields can be resolved by only one subgraph).")
@Retention(RUNTIME)
@Experimental("SmallRye GraphQL Federation is still subject to change.")
public @interface Shareable {
}
