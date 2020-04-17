package io.smallrye.graphql.client.typesafe.api;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Retention(RUNTIME)
@Target(TYPE)
public @interface GraphQlClientApi {
    String endpoint() default "";

    String configKey() default "";
}
