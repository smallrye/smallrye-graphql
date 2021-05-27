package io.smallrye.graphql.federation.api;

import static io.smallrye.graphql.api.DirectiveLocation.FIELD_DEFINITION;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.NonNull;

import io.smallrye.graphql.api.Directive;

/** <b><code>directive @provides(fields: _FieldSet!) on FIELD_DEFINITION</code></b> */
@Directive(on = FIELD_DEFINITION)
@Description("When resolving the annotated field, this service can provide additional, normally `@external` fields.")
@Retention(RUNTIME)
public @interface Provides {
    @NonNull
    String[] fields();
}
