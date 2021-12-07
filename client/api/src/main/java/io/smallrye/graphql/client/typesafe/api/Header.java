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
 * Annotate parameters, your method or the {@link GraphQLClientApi API} interface.
 * <p>
 * The `name` is always fixed, but the value can be either a `constant`
 * or the name of a `method` for dynamic values. It can't be both or neither.
 * The method is always a <code>static</code> method, either on the interface itself
 * (or one of the classes it's nested in) or any other class, when the method name is
 * prefixed by the fully qualified class name plus <code>.</code> and is accessible,
 * e.g. `java.lang.System.lineSeparator`.
 * <p>
 * In some cases, you may want to pass the value of a header as a parameter;
 * neither `constant` nor `method` will be allowed then.
 * <p>
 * The `name` can be derived from the name of the parameter or method. It's mandatory
 * for `constant` headers.
 * <p>
 * <code>@Header</code> annotations can be defined via <code>@Stereotype</code>s.
 */
@Retention(RUNTIME)
@Target({ TYPE, METHOD, PARAMETER })
@Repeatable(Headers.class)
public @interface Header {
    String name() default "";

    String constant() default "";

    String method() default "";
}
