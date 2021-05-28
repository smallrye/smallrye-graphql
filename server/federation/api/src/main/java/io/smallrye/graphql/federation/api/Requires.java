package io.smallrye.graphql.federation.api;

import static io.smallrye.graphql.api.DirectiveLocation.FIELD_DEFINITION;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

import io.smallrye.common.annotation.Experimental;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.NonNull;

import io.smallrye.graphql.api.Directive;

/** <b><code>directive @requires(fields: _FieldSet!) on FIELD_DEFINITION</code></b> */
@Directive(on = FIELD_DEFINITION)
@Description("In order to resolve the annotated field, this service needs these additional `@external` fields, " +
        "even when the client didn't request them.")
@Retention(RUNTIME)
@Experimental("SmallRye GraphQL Federation is still subject to change. " +
    "Additionally, this annotation is currently only a directive without explicit support from the extension.")
public @interface Requires {
    @NonNull
    String[] fields();
}
