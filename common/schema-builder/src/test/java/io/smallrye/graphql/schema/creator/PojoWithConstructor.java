package io.smallrye.graphql.schema.creator;

import org.eclipse.microprofile.graphql.Name;

public class PojoWithConstructor {

    String field;

    public PojoWithConstructor(
            @Name("Foo") final String field) {
        this.field = field;
    }
}
