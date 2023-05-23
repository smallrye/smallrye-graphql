package io.smallrye.graphql.schema.schemadirectives;

import static io.smallrye.graphql.api.DirectiveLocation.SCHEMA;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;

import org.eclipse.microprofile.graphql.NonNull;

import io.smallrye.graphql.api.Directive;

@Directive(on = { SCHEMA })
@Retention(RUNTIME)
@Repeatable(RepeatableSchemaDirective.RepeatableSchemaDirectives.class)
public @interface RepeatableSchemaDirective {
    @NonNull
    String name();

    @Retention(RUNTIME)
    @interface RepeatableSchemaDirectives {
        RepeatableSchemaDirective[] value();
    }
}
