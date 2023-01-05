package io.smallrye.graphql.api.federation;

import io.smallrye.common.annotation.Experimental;
import io.smallrye.graphql.api.Directive;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.NonNull;

import java.lang.annotation.Retention;

import static io.smallrye.graphql.api.DirectiveLocation.FIELD_DEFINITION;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * <b><code>directive @provides(fields: FieldSet!) on FIELD_DEFINITION</code></b>
 *
 * @see <a href="https://www.apollographql.com/docs/federation/federated-types/federated-directives/#provides">federation spec</a>
 */
@Directive(on = FIELD_DEFINITION)
@Description("Specifies a set of entity fields that a subgraph can resolve, but only at a particular schema path " +
             "(at other paths, the subgraph can't resolve those fields).\n" +
             "If a subgraph can always resolve a particular entity field, do not apply this directive.\n" +
             "Using this directive is always an optional optimization. It can reduce the total number of subgraphs " +
             "that your graph router needs to communicate with to resolve certain operations, which can improve performance.")
@Retention(RUNTIME)
@Experimental("SmallRye GraphQL Federation is still subject to change.")
public @interface Provides {
    @NonNull
    @Description("A GraphQL selection set (provided as a string) of object fields and subfields that the subgraph " +
                 "can resolve only at this query path.\n" +
                 "Examples:\n" +
                 "\"name\"\n" +
                 "\"name address\"\n" +
                 "\"... on Person { name address }\" (valid for fields that return a union or interface)")
    String fields();
}
