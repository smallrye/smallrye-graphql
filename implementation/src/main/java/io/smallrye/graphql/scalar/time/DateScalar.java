package io.smallrye.graphql.scalar.time;

import java.sql.Date;
import java.time.DateTimeException;
import java.time.LocalDate;

import io.smallrye.graphql.transformation.DateTransformer;

/**
 * Scalar for Date.
 * Both time api LocalDate and sql Date is supported
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class DateScalar extends AbstractDateScalar {

    public DateScalar() {
        super("Date", new DateConverter(), LocalDate.class, Date.class);
    }

    static class DateConverter implements Converter {
        private final DateTransformer dateTransformer = DateTransformer.transformer(); // default

        @Override
        public Object fromString(String value) {
            try {
                return dateTransformer.stringToDateType(value, LocalDate.class.getName()).orElse(null);
            } catch (DateTimeException dte) {
                // Let Strings through so that the formatting can kick in
                return value;
            }
        }
    }
}
