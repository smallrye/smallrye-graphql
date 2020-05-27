package io.smallrye.graphql.client.typesafe.api;

import static io.smallrye.graphql.client.typesafe.api.AuthorizationHeader.Type.BASIC;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * A specialized {@link Header} for <code>Authorization</code>.
 * Either {@link Type#BASIC BASIC} (default) or {@link Type#BEARER BEARER}.
 * Credentials are configured with MP Config.
 * <p>
 * <code>@Header</code> annotations can be defined via <code>@Steretype</code>s.
 */
@Retention(RUNTIME)
@Target({ TYPE, METHOD })
public @interface AuthorizationHeader {
    /**
     * The prefix of the config key to be used, plus <code>/mp-graphql/</code>.
     * <p>
     * If the prefix ends with <code>*</code>, <code>/mp-graphql/</code> will <em>not</em> be appended,
     * e.g. <code>@AuthorizationHeader(confPrefix = "org.superheroes.security.basic.*</code> will use
     * <code>org.superheroes.security.basic.username</code>, while <code>*</code> will use plain
     * <code>username</code>.
     * <p>
     * Defaults to the fully qualified name of the API interface or its {@link GraphQlClientApi#configKey()}.
     */
    String confPrefix() default "";

    /**
     * The first word of the <code>Authorization</code> header value, i.e. <code>Basic</code> or <code>Bearer</code>.
     * Also determines the config keys to use. See {@link Type#BASIC}/{@link Type#BEARER} for details.
     */
    Type type() default BASIC;

    enum Type {
        /**
         * The value is <code>"Basic " + base64(username + ":" + password)</code> where <code>username</code> and
         * <code>password</code> are configurations from MP Config, prefixed as defined with {@link #confPrefix()}.
         */
        BASIC,

        /**
         * The value is <code>"Bearer " + bearer</code> where <code>bearer</code> is a configurations from MP Config,
         * prefixed as defined with {@link #confPrefix()}.
         */
        BEARER
    }
}
