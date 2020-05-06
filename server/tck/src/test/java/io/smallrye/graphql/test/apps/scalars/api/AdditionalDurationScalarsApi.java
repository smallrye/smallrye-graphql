package io.smallrye.graphql.test.apps.scalars.api;

import java.time.Duration;
import java.time.Period;

import org.eclipse.microprofile.graphql.DefaultValue;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;
import org.eclipse.microprofile.graphql.Source;

@GraphQLApi
public class AdditionalDurationScalarsApi {

    @Query
    public AdditionalDurationScalars additionalDurationScalars() {
        return new AdditionalDurationScalars();
    }

    public Duration durationInput(@Source AdditionalDurationScalars additionalDurationScalars, Duration duration) {
        return duration;
    }

    public Duration durationDefault(@Source AdditionalDurationScalars additionalDurationScalars,
            @DefaultValue("PT1H2M3S") Duration duration) {
        return duration;
    }

    public Period periodInput(@Source AdditionalDurationScalars additionalDurationScalars, Period period) {
        return period;
    }

    public Period periodDefault(@Source AdditionalDurationScalars additionalDurationScalars,
            @DefaultValue("P1Y2M3D") Period period) {
        return period;
    }

}
