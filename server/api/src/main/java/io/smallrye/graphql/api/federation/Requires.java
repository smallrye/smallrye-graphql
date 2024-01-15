package io.smallrye.graphql.api.federation;

import static io.smallrye.graphql.api.DirectiveLocation.FIELD_DEFINITION;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.NonNull;

import io.smallrye.common.annotation.Experimental;
import io.smallrye.graphql.api.Directive;

/**
 * <b><code>directive @requires(fields: FieldSet!) on FIELD_DEFINITION</code></b>
 *
 * @see <a href="https://www.apollographql.com/docs/federation/federated-types/federated-directives/#requires">federation
 *      spec</a>
 */
@Directive(on = FIELD_DEFINITION)
@Description("Indicates that the resolver for a particular entity field depends on the values of other entity fields " +
        "that are resolved by other subgraphs. This tells the graph router that it needs to fetch the values " +
        "of those externally defined fields first, even if the original client query didn't request them.")
@Retention(RUNTIME)
@Experimental("SmallRye GraphQL Federation is still subject to change.")
public @interface Requires {
    @NonNull
    @Description("A GraphQL selection set (provided as a string) of @external object fields and subfields that this field " +
            "requires.\n" +
            "Examples:\n" +
            "\"name\"\n" +
            "\"name address\"\n" +
            "\"name organization { id }\"")
    String fields();
}
