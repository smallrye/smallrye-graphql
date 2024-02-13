package io.smallrye.graphql.api.federation.link;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

import org.eclipse.microprofile.graphql.NonNull;

/**
 * An element, possibly aliased, to import into the document.
 */
@Retention(RUNTIME)
public @interface Import {
    @NonNull
    String name();

    String as() default "";
}
