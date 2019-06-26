package io.leangen.graphql.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import graphql.introspection.Introspection;

@Ignore
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface GraphQLDirective {

    String name() default "";

    Introspection.DirectiveLocation[] locations() default {};
}
