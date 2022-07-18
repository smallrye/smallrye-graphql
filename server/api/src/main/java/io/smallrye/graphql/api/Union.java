package io.smallrye.graphql.api;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import io.smallrye.common.annotation.Experimental;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(TYPE)
@Experimental("Allow you to mark an interface as a GraphQL Union. Not covered by the specification. " +
    "Subject to change.")
public @interface Union {

  String value() default "";
}
