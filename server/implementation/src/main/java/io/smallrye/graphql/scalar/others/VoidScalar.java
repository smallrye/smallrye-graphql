package io.smallrye.graphql.scalar.others;

import graphql.schema.Coercing;
import io.smallrye.graphql.scalar.AbstractScalar;

public class VoidScalar extends AbstractScalar {
    public VoidScalar() {
        super("Void", new Coercing() {
            @Override
            public Object serialize(Object input) {
                return null;
            }
        }, Void.class, void.class);
    }
}
