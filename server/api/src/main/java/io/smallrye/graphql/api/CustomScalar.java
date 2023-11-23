package io.smallrye.graphql.api;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import io.smallrye.common.annotation.Experimental;

@Retention(RUNTIME)
@Target(TYPE)
@Experimental("Mark a type as a custom scalar with the given name in the GraphQL schema.")
public @interface CustomScalar {

    String value();
}
