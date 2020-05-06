package io.smallrye.graphql.scalar.time;

import java.time.Period;

public class PeriodScalar extends AbstractDateScalar {

    public PeriodScalar() {
        super("Period", Period.class);
    }

}
