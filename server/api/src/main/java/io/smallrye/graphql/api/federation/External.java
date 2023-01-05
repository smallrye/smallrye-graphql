package io.smallrye.graphql.api.federation;

import static io.smallrye.graphql.api.DirectiveLocation.FIELD_DEFINITION;
import static io.smallrye.graphql.api.DirectiveLocation.OBJECT;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

import org.eclipse.microprofile.graphql.Description;

import io.smallrye.common.annotation.Experimental;
import io.smallrye.graphql.api.Directive;

/**
 *  <b><code>directive @external on FIELD_DEFINITION | OBJECT</code></b>
 *
 * @see <a href="https://www.apollographql.com/docs/federation/federated-types/federated-directives/#external">federation spec</a>
 */
@Directive(on = {FIELD_DEFINITION, OBJECT})
@Description("Indicates that this subgraph usually can't resolve a particular object field, but it still needs to define " +
             "that field for other purposes.\n" +
             "This directive is always used in combination with another directive that references object fields, " +
             "such as @provides or @requires.")
@Retention(RUNTIME)
@Experimental("SmallRye GraphQL Federation is still subject to change.")
public @interface External {
}
