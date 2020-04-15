package io.smallrye.graphql.scalar.time;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Date;

import org.jboss.logging.Logger;

/**
 * Scalar for DateTime.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class DateTimeScalar extends AbstractDateScalar {
    private static final Logger LOG = Logger.getLogger(DateTimeScalar.class.getName());

    public DateTimeScalar() {
        super("DateTime", LocalDateTime.class, Date.class, Timestamp.class, ZonedDateTime.class, OffsetDateTime.class);
    }

    //    @Override
    //    public Object transform(Object input, Argument argument) {
    //
    //        if (argument.getType().name().equals(Classes.LOCALDATETIME)) {
    //            return transformToLocalDateTime(argument.getName(), input.toString(), argument.getType(),
    //                    argument.getAnnotations());
    //        } else if (argument.getType().name().equals(Classes.UTIL_DATE)) {
    //            LocalDateTime localDateTime = transformToLocalDateTime(argument.getName(), input.toString(), argument.getType(),
    //                    argument.getAnnotations());
    //            return toUtilDate(localDateTime);
    //        } else if (argument.getType().name().equals(Classes.SQL_TIMESTAMP)) {
    //            LocalDateTime localDateTime = transformToLocalDateTime(argument.getName(), input.toString(), argument.getType(),
    //                    argument.getAnnotations());
    //            return java.sql.Timestamp.valueOf(localDateTime);
    //        } else if (argument.getType().name().equals(Classes.OFFSETDATETIME)) {
    //            return transformToOffsetDateTime(argument.getName(), input.toString(), argument.getType(),
    //                    argument.getAnnotations());
    //        } else if (argument.getType().name().equals(Classes.ZONEDDATETIME)) {
    //            return transformToZonedDateTime(argument.getName(), input.toString(), argument.getType(),
    //                    argument.getAnnotations());
    //        } else {
    //            LOG.warn("Can not transform type [" + argument.getType().name() + "] with DateTimeScalar");
    //            return input;
    //        }
    //    }

    //    private Date toUtilDate(LocalDateTime dateToConvert) {
    //        return java.util.Date
    //                .from(dateToConvert.atZone(ZoneId.systemDefault())
    //                        .toInstant());
    //    }

    //    private LocalDateTime transformToLocalDateTime(String name, String input, Type type, Annotations annotations) {
    //        try {
    //            DateTimeFormatter dateFormat = formatHelper.getDateFormat(type, annotations);
    //            return LocalDateTime.parse(input, dateFormat);
    //        } catch (DateTimeParseException dtpe) {
    //            throw new TransformException(dtpe, this, name, input);
    //        }
    //    }

    //    private ZonedDateTime transformToZonedDateTime(String name, String input, Type type, Annotations annotations) {
    //        try {
    //            DateTimeFormatter dateFormat = formatHelper.getDateFormat(type, annotations);
    //            return ZonedDateTime.parse(input, dateFormat);
    //        } catch (DateTimeParseException dtpe) {
    //            throw new TransformException(dtpe, this, name, input);
    //        }
    //    }

    //    private OffsetDateTime transformToOffsetDateTime(String name, String input, Type type, Annotations annotations) {
    //        try {
    //            DateTimeFormatter dateFormat = formatHelper.getDateFormat(type, annotations);
    //            return OffsetDateTime.parse(input, dateFormat);
    //        } catch (DateTimeParseException dtpe) {
    //            throw new TransformException(dtpe, this, name, input);
    //        }
    //    }
}
