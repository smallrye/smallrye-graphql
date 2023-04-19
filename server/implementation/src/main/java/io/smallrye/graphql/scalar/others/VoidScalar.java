package io.smallrye.graphql.scalar.others;

import io.smallrye.graphql.scalar.time.AbstractDateScalar;

public class VoidScalar extends AbstractDateScalar {
    public VoidScalar() {
        super("Void", Void.class, void.class);
    }
}
