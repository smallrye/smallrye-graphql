package io.smallrye.graphql.schema;

import static io.smallrye.graphql.api.DirectiveLocation.ENUM;
import static io.smallrye.graphql.api.DirectiveLocation.ENUM_VALUE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

import io.smallrye.graphql.api.Directive;

@Retention(RUNTIME)
@Directive(on = { ENUM, ENUM_VALUE })
public @interface EnumDirective {
}
