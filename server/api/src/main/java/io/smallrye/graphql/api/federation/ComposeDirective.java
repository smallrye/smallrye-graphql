package io.smallrye.graphql.api.federation;

import static io.smallrye.graphql.api.DirectiveLocation.SCHEMA;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;

import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.NonNull;

import io.smallrye.common.annotation.Experimental;
import io.smallrye.graphql.api.Directive;
import io.smallrye.graphql.api.federation.ComposeDirective.ComposeDirectives;

/**
 * <b><code>directive @composeDirective(name: String!) repeatable on SCHEMA</code></b>
 *
 * @see <a href="https://www.apollographql.com/docs/federation/federated-types/federated-directives/#composedirective">
 *      federation spec</a>
 */
@Directive(on = { SCHEMA })
@Description("Indicates to composition that all uses of a particular custom type system directive in the subgraph schema" +
        " should be preserved in the supergraph schema (by default, composition omits most directives from the supergraph" +
        " schema).")
@Retention(RUNTIME)
@Repeatable(ComposeDirectives.class)
@Experimental("SmallRye GraphQL Federation is still subject to change.")
public @interface ComposeDirective {
    @NonNull
    @Description("The name (including the leading @) of the directive to preserve during composition.")
    String name();

    @Retention(RUNTIME)
    @interface ComposeDirectives {
        ComposeDirective[] value();
    }
}
