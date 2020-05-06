package io.smallrye.graphql.transformation;

import java.time.Duration;

public class DurationTransformer implements Transformer {
    @Override
    public Object in(final Object o) throws Exception {
        return Duration.parse(o.toString());
    }

    @Override
    public Object out(final Object o) {
        return o.toString();
    }
}
