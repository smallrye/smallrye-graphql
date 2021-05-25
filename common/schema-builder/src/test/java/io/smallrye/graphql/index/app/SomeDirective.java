package io.smallrye.graphql.index.app;

import static io.smallrye.graphql.api.DirectiveLocation.FIELD;
import static io.smallrye.graphql.api.DirectiveLocation.INTERFACE;
import static io.smallrye.graphql.api.DirectiveLocation.OBJECT;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

import io.smallrye.graphql.api.Directive;

@Directive(on = { OBJECT, INTERFACE, FIELD })
@Retention(RUNTIME)
public @interface SomeDirective {
    String[] value();
}
