package io.smallrye.graphql.api.federation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import io.smallrye.common.annotation.Experimental;

@Target(ElementType.METHOD)
@Retention(RUNTIME)
@Experimental("Resolver method without creating query method")
public @interface Resolver {
}
