package io.smallrye.graphql.schema.schemadirectives;

import static io.smallrye.graphql.api.DirectiveLocation.SCHEMA;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

import io.smallrye.graphql.api.Directive;

@Directive(on = { SCHEMA })
@Retention(RUNTIME)
public @interface NonRepeatableSchemaDirective {
}
