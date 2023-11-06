package io.smallrye.graphql.tests.client.typesafe.directives;

import static io.smallrye.graphql.api.DirectiveLocation.VARIABLE_DEFINITION;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;

import io.smallrye.graphql.api.Directive;

@Retention(RUNTIME)
@Directive(on = { VARIABLE_DEFINITION })
@Repeatable(VariableDefinitionDirective.VariableDefinitionDirectives.class)
public @interface VariableDefinitionDirective {
    String fields() default "";

    @Retention(RUNTIME)
    @interface VariableDefinitionDirectives {
        VariableDefinitionDirective[] value();
    }
}
