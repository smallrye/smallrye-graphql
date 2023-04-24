package io.smallrye.graphql.scalar.time;

import java.sql.Date;
import java.time.LocalDate;

/**
 * Scalar for Date.
 * Both time api LocalDate and sql Date is supported
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class DateScalar extends AbstractDateScalar {

    public DateScalar() {
        super("Date", LocalDate.class, Date.class);
    }

}
