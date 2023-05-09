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

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;

import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.NonNull;

import io.smallrye.common.annotation.Experimental;
import io.smallrye.graphql.api.Directive;
import io.smallrye.graphql.api.federation.Tag.Tags;

/**
 * <b><code>directive @tag(name: String!) repeatable on FIELD_DEFINITION | INTERFACE | OBJECT | UNION |
 * ARGUMENT_DEFINITION | SCALAR | ENUM | ENUM_VALUE | INPUT_OBJECT | INPUT_FIELD_DEFINITION</code></b>
 *
 * @see <a href="https://www.apollographql.com/docs/federation/federated-types/federated-directives/#tag">federation spec</a>
 */
@Directive(on = { FIELD_DEFINITION, INTERFACE, OBJECT, UNION, ARGUMENT_DEFINITION, SCALAR, ENUM, ENUM_VALUE,
        INPUT_OBJECT, INPUT_FIELD_DEFINITION })
@Retention(RUNTIME)
@Description("Applies arbitrary string metadata to a schema location. Custom tooling can use this metadata" +
        " during any step of the schema delivery flow, including composition, static analysis, and" +
        " documentation. Apollo Studio's enterprise contracts feature uses @tag with its inclusion and" +
        " exclusion filters.")
@Experimental("SmallRye GraphQL Federation is still subject to change.")
@Repeatable(Tags.class)
public @interface Tag {
    @NonNull
    @Description("The tag name to apply.")
    String name();

    @Retention(RUNTIME)
    @interface Tags {
        Tag[] value();
    }
}
