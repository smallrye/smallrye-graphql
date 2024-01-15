package io.smallrye.graphql.api.federation;

import static io.smallrye.graphql.api.DirectiveLocation.ARGUMENT_DEFINITION;
import static io.smallrye.graphql.api.DirectiveLocation.ENUM;
import static io.smallrye.graphql.api.DirectiveLocation.ENUM_VALUE;
import static io.smallrye.graphql.api.DirectiveLocation.FIELD_DEFINITION;
import static io.smallrye.graphql.api.DirectiveLocation.INPUT_FIELD_DEFINITION;
import static io.smallrye.graphql.api.DirectiveLocation.INPUT_OBJECT;
import static io.smallrye.graphql.api.DirectiveLocation.INTERFACE;
import static io.smallrye.graphql.api.DirectiveLocation.OBJECT;
import static io.smallrye.graphql.api.DirectiveLocation.SCALAR;
import static io.smallrye.graphql.api.DirectiveLocation.UNION;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

import org.eclipse.microprofile.graphql.Description;

import io.smallrye.common.annotation.Experimental;
import io.smallrye.graphql.api.Directive;

/**
 * <b><code>directive @inaccessible on FIELD_DEFINITION | INTERFACE | OBJECT | UNION | ARGUMENT_DEFINITION | SCALAR |
 * ENUM | ENUM_VALUE | INPUT_OBJECT | INPUT_FIELD_DEFINITION</code></b>
 *
 * @see <a href="https://www.apollographql.com/docs/federation/federated-types/federated-directives/#inaccessible">federation
 *      spec</a>
 */
@Directive(on = { FIELD_DEFINITION, INTERFACE, OBJECT, UNION, ARGUMENT_DEFINITION, SCALAR, ENUM, ENUM_VALUE, INPUT_OBJECT,
        INPUT_FIELD_DEFINITION })
@Retention(RUNTIME)
@Description("Indicates that a definition in the subgraph schema should be omitted from the router's API schema," +
        " even if that definition is also present in other subgraphs." +
        " This means that the field is not exposed to clients at all.")
@Experimental("SmallRye GraphQL Federation is still subject to change.")
public @interface Inaccessible {
}
