package io.smallrye.graphql.test.apps.scalars.api;

import java.time.Duration;
import java.time.Period;

public class AdditionalDurationScalars {

    private final Duration duration;
    private final Period period;

    public AdditionalDurationScalars() {
        this.duration = Duration.parse("PT1H2M3S");
        this.period = Period.parse("P1Y2M3D");
    }

    public Duration getDuration() {
        return duration;
    }

    public Period getPeriod() {
        return period;
    }
}
