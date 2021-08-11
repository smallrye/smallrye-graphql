package io.smallrye.graphql.api.federation;

import static io.smallrye.graphql.api.DirectiveLocation.INTERFACE;
import static io.smallrye.graphql.api.DirectiveLocation.OBJECT;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.NonNull;

import io.smallrye.common.annotation.Experimental;
import io.smallrye.graphql.api.Directive;

/** <b><code>directive @key(fields: _FieldSet!) on OBJECT | INTERFACE</code></b> */
@Directive(on = { OBJECT, INTERFACE })
@Description("The @key directive is used to indicate a combination of fields that can be used to uniquely identify " +
        "and fetch an object or interface.")
@Retention(RUNTIME)
@Experimental("SmallRye GraphQL Federation is still subject to change.")
public @interface Key {
    @NonNull
    String[] fields();
}
