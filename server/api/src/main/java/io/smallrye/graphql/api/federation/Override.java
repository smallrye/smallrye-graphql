package io.smallrye.graphql.api.federation;

import static io.smallrye.graphql.api.DirectiveLocation.FIELD_DEFINITION;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.NonNull;

import io.smallrye.common.annotation.Experimental;
import io.smallrye.graphql.api.Directive;

/**
 * <b><code>directive @override(from: String!, label: String) on FIELD_DEFINITION</code></b>
 *
 * @see <a href="https://www.apollographql.com/docs/federation/federated-types/federated-directives/#override">federation
 *      spec</a>
 */
@Directive(on = { FIELD_DEFINITION })
@Retention(RUNTIME)
@Description("Indicates that an object field is now resolved by this subgraph instead of another subgraph where" +
        " it's also defined. This enables you to migrate a field from one subgraph to another.\n" +
        "You can apply @override to entity fields and fields of the root operation types (such as Query and Mutation).")
@Experimental("SmallRye GraphQL Federation is still subject to change.")
public @interface Override {
    @NonNull
    @Description("The name of the other subgraph that no longer resolves the field.\n" +
            "If you're performing composition with managed federation, this must match the name of the subgraph in Apollo Studio.\n"
            +
            "If you're performing composition with the Rover CLI, this must match the name of the subgraph in the YAML config file you provide to rover supergraph compose.")
    String from();

    @Description("A string of arbitrary arguments. Supported in this release:\n" +
            "percent(<percent-value>) - The percentage of traffic for the field that's resolved by this subgraph. The" +
            " remaining percentage is resolved by the other (from) subgraph.")
    String label() default "";
}
