package io.smallrye.graphql.schema;

import static io.smallrye.graphql.api.DirectiveLocation.INPUT_FIELD_DEFINITION;
import static io.smallrye.graphql.api.DirectiveLocation.INPUT_OBJECT;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

import io.smallrye.graphql.api.Directive;

@Retention(RUNTIME)
@Directive(on = { INPUT_OBJECT, INPUT_FIELD_DEFINITION })
public @interface InputDirective {
}
