package io.smallrye.graphql.api;

import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import io.smallrye.common.annotation.Experimental;

@Retention(RUNTIME)
@Target({ TYPE, PACKAGE })
@Experimental("Allow you to mark all fields in a class/package as non-null. Not covered by the specification. " +
        "Subject to change.")
public @interface DefaultNonNull {

}
