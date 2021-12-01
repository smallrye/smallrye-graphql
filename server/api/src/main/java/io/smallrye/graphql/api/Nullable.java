package io.smallrye.graphql.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.smallrye.common.annotation.Experimental;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE_USE, ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER })
@Documented
@Experimental("Allow you to mark a fields as nullable, overrides @AllNonNull. Not covered by the specification. " +
        "Subject to change.")
public @interface Nullable {

}
