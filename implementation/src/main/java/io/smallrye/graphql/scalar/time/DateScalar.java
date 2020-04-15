package io.smallrye.graphql.scalar.time;

import java.sql.Date;
import java.time.LocalDate;

import org.jboss.logging.Logger;

/**
 * Scalar for Date.
 * Both time api LocalDate and sql Date is supported
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class DateScalar extends AbstractDateScalar {
    private static final Logger LOG = Logger.getLogger(DateScalar.class.getName());

    public DateScalar() {
        super("Date", LocalDate.class, Date.class);
    }

    //    @Override
    //    public Object transform(Object input, Argument argument) {
    //        LocalDate localDate = transformToLocalDate(argument.getName(), input.toString(), argument.getType(),
    //                argument.getAnnotations());
    //
    //        if (argument.getType().name().equals(Classes.LOCALDATE)) {
    //            return localDate;
    //        } else if (argument.getType().name().equals(Classes.SQL_DATE)) {
    //            return Date.valueOf(localDate);
    //        } else {
    //            LOG.warn("Can not transform type [" + argument.getType().name() + "] with DateScalar");
    //            return input;
    //        }
    //    }

    //    private LocalDate transformToLocalDate(String name, String input, Type type, Annotations annotations) {
    //        try {
    //            DateTimeFormatter dateFormat = formatHelper.getDateFormat(type, annotations);
    //            return LocalDate.parse(input, dateFormat);
    //        } catch (DateTimeParseException dtpe) {
    //            throw new TransformException(dtpe, this, name, input);
    //        }
    //
    //    }

}
