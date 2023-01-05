package io.smallrye.graphql.api.federation;

import static io.smallrye.graphql.api.DirectiveLocation.INTERFACE;
import static io.smallrye.graphql.api.DirectiveLocation.OBJECT;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;

import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.NonNull;

import io.smallrye.common.annotation.Experimental;
import io.smallrye.graphql.api.Directive;
import io.smallrye.graphql.api.federation.Key.Keys;

/**
 * <b><code>directive @key(fields: FieldSet!) repeatable on OBJECT | INTERFACE</code></b>
 *
 * @see <a href="https://www.apollographql.com/docs/federation/federated-types/federated-directives/#key">federation spec</a>
 */
@Directive(on = {OBJECT, INTERFACE})
@Description("Designates an object type as an entity and specifies its key fields (a set of fields that the subgraph " +
             "can use to uniquely identify any instance of the entity). You can apply multiple @key directives to " +
             "a single entity (to specify multiple valid sets of key fields).")
@Retention(RUNTIME)
@Repeatable(Keys.class)
@Experimental("SmallRye GraphQL Federation is still subject to change.")
public @interface Key {
    @NonNull
    @Description("A GraphQL selection set (provided as a string) of fields and subfields that contribute " +
                 "to the entity's primary key.\n" +
                 "Examples:\n" +
                 "\"id\"\n" +
                 "\"username region\"\n" +
                 "\"name organization { id }\"")
    String fields();

    @Retention(RUNTIME) @interface Keys {
        Key[] value();
    }
}
