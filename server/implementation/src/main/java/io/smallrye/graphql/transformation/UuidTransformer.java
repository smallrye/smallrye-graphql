package io.smallrye.graphql.transformation;

import java.util.UUID;

public class UuidTransformer implements Transformer {

    @Override
    public Object in(final Object o) {
        return UUID.fromString(o.toString());
    }

    @Override
    public Object out(final Object o) {
        return o.toString();
    }
}
