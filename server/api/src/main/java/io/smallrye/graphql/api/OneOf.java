package io.smallrye.graphql.api;

import static io.smallrye.graphql.api.DirectiveLocation.INPUT_OBJECT;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

import org.eclipse.microprofile.graphql.Description;

import io.smallrye.common.annotation.Experimental;

/**
 * <b><code>directive @oneOf on INPUT_OBJECT</code></b>
 */
@Directive(on = INPUT_OBJECT)
@Description("Indicates an Input Object is a OneOf Input Object.")
@Retention(RUNTIME)
@Experimental("The directive is currently labelled as experimental inside graphql-java.")
public @interface OneOf {
}
