package io.smallrye.graphql.client.typesafe.api;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotate your GraphQL Client <code>interface</code> as {@link GraphQLClientApi},
 * so CDI can build and inject it for you.
 * <p>
 * Example:
 * 
 * <pre>
 * &#64;GraphQLClientApi
 * public interface SuperHeroesApi {
 *     List&lt;Hero&gt; allHeroes();
 * }
 *
 * &#64;Inject
 * SuperHeroesApi superHeroesApi;
 * </pre>
 *
 * You can optionally add some fallback configuration with this annotation.
 */
@Documented
@Retention(RUNTIME)
@Target(TYPE)
public @interface GraphQLClientApi {
    /**
     * The URL where the GraphQL service is listening
     */
    String endpoint() default "";

    /**
     * The base key used to read configuration values. Defaults to the fully qualified name of the API interface.
     */
    String configKey() default "";
}
