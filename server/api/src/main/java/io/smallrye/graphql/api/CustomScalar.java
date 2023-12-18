package io.smallrye.graphql.api;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import io.smallrye.common.annotation.Experimental;

/**
 * Allows for definition of custom graphql scalars.  Types with this annotation should extend one of
 * CustomStringScalar, CustomIntScalar, or CustomFloatScalar.  Additionally, the Type should provide
 * a single argument constructor taking the associated type(String, BigInteger, BigDecimal).
 */
@Retention(RUNTIME)
@Target(TYPE)
@Experimental("Mark a type as a custom scalar with the given name in the GraphQL schema.")
public @interface CustomScalar {

    String value();
}
