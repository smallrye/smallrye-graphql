package io.smallrye.graphql.api;

import static io.smallrye.graphql.api.DirectiveLocation.ARGUMENT_DEFINITION;
import static io.smallrye.graphql.api.DirectiveLocation.ENUM_VALUE;
import static io.smallrye.graphql.api.DirectiveLocation.FIELD_DEFINITION;
import static io.smallrye.graphql.api.DirectiveLocation.INPUT_FIELD_DEFINITION;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD })
@Directive(on = {
        FIELD_DEFINITION,
        ARGUMENT_DEFINITION,
        INPUT_FIELD_DEFINITION,
        ENUM_VALUE
})
public @interface Deprecated {
    String reason() default "";
}
