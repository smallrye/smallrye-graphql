package io.smallrye.graphql.api.federation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;

import io.smallrye.common.annotation.Experimental;

@java.lang.annotation.Target(ElementType.PARAMETER)
@Retention(RUNTIME)
@Experimental("Marks a resolver method parameter as the target entity representation")
public @interface Target {
}
