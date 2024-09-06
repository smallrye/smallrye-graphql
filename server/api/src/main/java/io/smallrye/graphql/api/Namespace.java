package io.smallrye.graphql.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.smallrye.common.annotation.Experimental;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
@Documented
@Experimental("Grouping APIs by nested namespaces")
public @interface Namespace {
    /**
     * @return Array of nested namespaces
     */
    String[] value();
}
