package io.smallrye.graphql.tests.client.typesafe.directives;

import static io.smallrye.graphql.api.DirectiveLocation.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;

import org.eclipse.microprofile.graphql.NonNull;

import io.smallrye.graphql.api.Directive;

@Retention(RUNTIME)
@Directive(on = { FIELD })
@Repeatable(FieldDirective.FieldDirectives.class)
public @interface FieldDirective {
    @NonNull
    int[] fields();

    @Retention(RUNTIME)
    @interface FieldDirectives {
        FieldDirective[] value();
    }
}
