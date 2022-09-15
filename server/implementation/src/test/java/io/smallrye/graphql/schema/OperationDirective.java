package io.smallrye.graphql.schema;

import static io.smallrye.graphql.api.DirectiveLocation.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

import io.smallrye.graphql.api.Directive;

@Retention(RUNTIME)
@Directive(on = { QUERY, MUTATION, SUBSCRIPTION })
public @interface OperationDirective {
}
