package io.smallrye.graphql.schema;

import static io.smallrye.graphql.api.DirectiveLocation.INTERFACE;
import static io.smallrye.graphql.api.DirectiveLocation.OBJECT;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

import org.eclipse.microprofile.graphql.Description;

import io.smallrye.graphql.api.Directive;

@Directive(on = { OBJECT, INTERFACE })
@Description("test-description")
@Retention(RUNTIME)
public @interface IntArrayTestDirective {
    int[] value();
}
