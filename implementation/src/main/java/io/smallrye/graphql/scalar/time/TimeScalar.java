package io.smallrye.graphql.scalar.time;

import java.sql.Time;
import java.time.LocalTime;
import java.time.OffsetTime;

import org.jboss.logging.Logger;

/**
 * Scalar for Time.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class TimeScalar extends AbstractDateScalar {
    private static final Logger LOG = Logger.getLogger(TimeScalar.class.getName());

    public TimeScalar() {
        super("Time", LocalTime.class, Time.class, OffsetTime.class);
    }

    //    @Override
    //    public Object transform(Object input, Argument argument) {
    //
    //        if (argument.getType().name().equals(Classes.LOCALTIME)) {
    //            return transformToLocalTime(argument.getName(), input.toString(), argument.getType(),
    //                    argument.getAnnotations());
    //        } else if (argument.getType().name().equals(Classes.SQL_TIME)) {
    //            LocalTime localTime = transformToLocalTime(argument.getName(), input.toString(), argument.getType(),
    //                    argument.getAnnotations());
    //            return java.sql.Time.valueOf(localTime);
    //        } else if (argument.getType().name().equals(Classes.OFFSETTIME)) {
    //            return transformToOffsetTime(argument.getName(), input.toString(), argument.getType(),
    //                    argument.getAnnotations());
    //        } else {
    //            LOG.warn("Can not transform type [" + argument.getType().name() + "] with TimeScalar");
    //            return input;
    //        }
    //    }

    //    private LocalTime transformToLocalTime(String name, String input, Type type, Annotations annotations) {
    //        try {
    //            DateTimeFormatter dateFormat = formatHelper.getDateFormat(type, annotations);
    //            return LocalTime.parse(input, dateFormat);
    //        } catch (DateTimeParseException dtpe) {
    //            throw new TransformException(dtpe, this, name, input);
    //        }
    //    }

    //    private OffsetTime transformToOffsetTime(String name, String input, Type type, Annotations annotations) {
    //        try {
    //            DateTimeFormatter dateFormat = formatHelper.getDateFormat(type, annotations);
    //            return OffsetTime.parse(input, dateFormat);
    //        } catch (DateTimeParseException dtpe) {
    //            throw new TransformException(dtpe, this, name, input);
    //        }
    //    }
}
