package io.smallrye.graphql.index.app;

import static io.smallrye.graphql.api.DirectiveLocation.FIELD_DEFINITION;
import static io.smallrye.graphql.api.DirectiveLocation.INPUT_FIELD_DEFINITION;
import static io.smallrye.graphql.api.DirectiveLocation.INPUT_OBJECT;
import static io.smallrye.graphql.api.DirectiveLocation.INTERFACE;
import static io.smallrye.graphql.api.DirectiveLocation.OBJECT;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

import io.smallrye.graphql.api.Directive;

@Directive(on = { OBJECT, INTERFACE, FIELD_DEFINITION, INPUT_OBJECT, INPUT_FIELD_DEFINITION })
@Retention(RUNTIME)
public @interface SomeDirective {
    String[] value();
}
