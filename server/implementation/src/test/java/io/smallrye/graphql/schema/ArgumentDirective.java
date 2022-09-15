package io.smallrye.graphql.schema;

import static io.smallrye.graphql.api.DirectiveLocation.ARGUMENT_DEFINITION;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

import io.smallrye.graphql.api.Directive;

@Retention(RUNTIME)
@Directive(on = ARGUMENT_DEFINITION)
public @interface ArgumentDirective {
}
