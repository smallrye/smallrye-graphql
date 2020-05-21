package io.smallrye.graphql.client.typesafe.api;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Adds a custom header to the http requests sent out by the GraphQL Client.
 * Annotate your method or the {@link GraphQlClientApi API} interface.
 * <p>
 * The `name` is always fixed, but the value can be either a `constant`
 * or the name of a `method` for dynamic values. It can't be both or neither.
 * The method is always a static method, either on the interface itself
 * or on a fully qualified class.
 * <p>
 * In rare cases, you may want to pass the value of a header as a parameter;
 * neither `constant` nor `method` will be allowed then.
 */
@Retention(RUNTIME)
@Target({ TYPE, METHOD, PARAMETER })
@Repeatable(Headers.class)
public @interface Header {
    String name();

    String constant() default "";

    String method() default "";
}
