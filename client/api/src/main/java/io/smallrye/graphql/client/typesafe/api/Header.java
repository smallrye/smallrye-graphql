package io.smallrye.graphql.client.typesafe.api;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;

/**
 * TODO complete javadoc
 *
 * 1. method (`constant` xor `method`)
 * 2. interface (`constant` xor `method`)
 * 3. param (neither `constant` nor `method`)
 *
 * method = static method on the interface or fully qualified
 */
@Retention(RUNTIME)
@Repeatable(Headers.class)
public @interface Header {
    String name();

    String constant() default "";

    String method() default "";
}
