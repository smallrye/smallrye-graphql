package io.smallrye.graphql.scalar.time;

import java.sql.Timestamp;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Date;

import io.smallrye.graphql.transformation.DateTransformer;

/**
 * Scalar for DateTime.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class DateTimeScalar extends AbstractDateScalar {

    public DateTimeScalar() {
        super("DateTime", new DateTimeConverter(), LocalDateTime.class, Date.class, Timestamp.class, ZonedDateTime.class,
                OffsetDateTime.class);
    }

    static class DateTimeConverter implements Converter {
        private final DateTransformer dateTransformer = DateTransformer.transformer(); // default

        @Override
        public Object fromString(String value) {
            try {
                return dateTransformer.stringToDateType(value, LocalDateTime.class.getName()).orElse(null);
            } catch (DateTimeException dte) {
                // Let Strings through so that the formatting can kick in
                return value;
            }
        }
    }
}
