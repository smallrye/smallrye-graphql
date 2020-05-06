package io.smallrye.graphql.scalar.time;

import java.time.Duration;

public class DurationScalar extends AbstractDateScalar {

    public DurationScalar() {
        super("Duration", Duration.class);
    }

}
