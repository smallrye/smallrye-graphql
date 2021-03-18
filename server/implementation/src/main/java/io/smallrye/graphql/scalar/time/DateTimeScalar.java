package io.smallrye.graphql.scalar.time;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Date;

/**
 * Scalar for DateTime.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class DateTimeScalar extends AbstractDateScalar {

    public DateTimeScalar() {
        super("DateTime", LocalDateTime.class, Date.class, Timestamp.class, ZonedDateTime.class,
                OffsetDateTime.class, Instant.class);
    }

}
