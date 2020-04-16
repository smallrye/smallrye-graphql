package io.smallrye.graphql.scalar.time;

import java.sql.Time;
import java.time.LocalTime;
import java.time.OffsetTime;

/**
 * Scalar for Time.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class TimeScalar extends AbstractDateScalar {

    public TimeScalar() {
        super("Time", LocalTime.class, Time.class, OffsetTime.class);
    }
}
