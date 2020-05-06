package io.smallrye.graphql.transformation;

import java.time.Period;

public class PeriodTransformer implements Transformer {
    @Override
    public Object in(final Object o) throws Exception {
        return Period.parse(o.toString());
    }

    @Override
    public Object out(final Object o) {
        return o.toString();
    }
}
