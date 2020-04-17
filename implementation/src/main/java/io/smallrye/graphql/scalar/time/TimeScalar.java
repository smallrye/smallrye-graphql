package io.smallrye.graphql.scalar.time;

import java.sql.Time;
import java.time.DateTimeException;
import java.time.LocalTime;
import java.time.OffsetTime;

import io.smallrye.graphql.transformation.DateTransformer;

/**
 * Scalar for Time.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class TimeScalar extends AbstractDateScalar {

    public TimeScalar() {
        super("Time", new TimeConverter(), LocalTime.class, Time.class, OffsetTime.class);
    }

    static class TimeConverter implements Converter {
        private final DateTransformer dateTransformer = DateTransformer.transformer(); // default

        @Override
        public Object fromString(String value) {
            try {
                return dateTransformer.stringToDateType(value, LocalTime.class.getName()).orElse(null);
            } catch (DateTimeException dte) {
                // Let Strings through so that the formatting can kick in
                return value;
            }
        }
    }
}
