package io.smallrye.graphql.execution.datafetcher;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;

import org.jboss.logging.Logger;

import io.smallrye.graphql.execution.Classes;
import io.smallrye.graphql.schema.model.Field;
import io.smallrye.graphql.schema.model.TransformInfo;

/**
 * Transform data.
 * Format on the way out and Parse on the way in
 * 
 * TODO: Consider caching created transformers ?
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class Transformer {
    private static final Logger LOG = Logger.getLogger(Transformer.class.getName());

    private DateTimeFormatter dateTimeFormatter = null;
    private NumberFormat numberFormat = null;

    private final Field field;

    public static Transformer transformer(Field field) {
        if (field.getTransformInfo().isPresent()) {
            return new Transformer(field);
        }
        return null;
    }

    private Transformer(Field field) {
        this.field = field;
        init(field.getTransformInfo());
    }

    private void init(Optional<TransformInfo> maybeFormat) {
        if (maybeFormat.isPresent()) {
            TransformInfo format = maybeFormat.get();

            if (format.getType().equals(TransformInfo.Type.NUMBER)) {
                this.numberFormat = getNumberFormat(format);
            } else if (format.getType().equals(TransformInfo.Type.DATE)) {
                this.dateTimeFormatter = getDateFormat(format);
            }
        }
    }

    // Parsing (on the way in)

    /**
     * Check if the input is valid and then parse with the correct transformer
     * 
     * @param input the value to be transformed
     * @return the transformed result
     */
    public Object parseInput(Object input) {

        if (Classes.isCollection(input)) {
            throw new RuntimeException("Can not parse [" + input + "] of type [" + input.getClass().getName() + "]");
        } else if (input != null && dateTimeFormatter != null) {
            return parseDateInput(input);
        } else if (input != null && numberFormat != null) {
            return parseNumberInput(input);
        }
        return input; // default
    }

    /**
     * Parse a date input
     * 
     * @param input the formatted date
     * @return date
     */
    private Object parseDateInput(Object input) {
        String className = field.getReference().getClassName();
        // Date
        if (className.equals(Classes.LOCALDATE)) {
            return LocalDate.parse(input.toString(), dateTimeFormatter);
        } else if (className.equals(Classes.SQL_DATE)) {
            LocalDate localdate = LocalDate.parse(input.toString(), dateTimeFormatter);
            return java.sql.Date.valueOf(localdate);

            // Date time    
        } else if (className.equals(Classes.LOCALDATETIME)) {
            return LocalDateTime.parse(input.toString(), dateTimeFormatter);
        } else if (className.equals(Classes.UTIL_DATE)) {
            LocalDateTime localdatetime = LocalDateTime.parse(input.toString(), dateTimeFormatter);
            return java.util.Date.from(localdatetime.atZone(ZoneId.systemDefault()).toInstant());
        } else if (className.equals(Classes.SQL_TIMESTAMP)) {
            LocalDateTime localdatetime = LocalDateTime.parse(input.toString(), dateTimeFormatter);
            return java.sql.Timestamp.valueOf(localdatetime);
        } else if (className.equals(Classes.OFFSETDATETIME)) {
            return OffsetDateTime.parse(input.toString(), dateTimeFormatter);
        } else if (className.equals(Classes.ZONEDDATETIME)) {
            return ZonedDateTime.parse(input.toString(), dateTimeFormatter);

            // Time    
        } else if (className.equals(Classes.LOCALTIME)) {
            return LocalTime.parse(input.toString(), dateTimeFormatter);
        } else if (className.equals(Classes.SQL_TIME)) {
            LocalTime localtime = LocalTime.parse(input.toString(), dateTimeFormatter);
            return java.sql.Time.valueOf(localtime);
        } else if (className.equals(Classes.OFFSETTIME)) {
            return OffsetTime.parse(input.toString(), dateTimeFormatter);

            // Unsupported
        } else {
            TemporalAccessor temporalAccessor = dateTimeFormatter.parse(input.toString());
            return temporalAccessor;
        }

    }

    /**
     * Parse a number input
     * 
     * @param input the formatted number
     * @return number
     */
    private Object parseNumberInput(Object input) {
        try {
            return numberFormat.parse(input.toString());
        } catch (ParseException ex) {
            LOG.warn("Could not parse [" + input.toString() + "]", ex);
            return input;
        }
    }

    // Formatting (on the way out)

    /**
     * Check if the output is valid and then format with the correct transformer
     * 
     * @param output the value to be transformed
     * @return the transformed result
     */
    public Object formatOutput(Object output) {
        if (Classes.isCollection(output)) {
            throw new RuntimeException("Can not format [" + output + "] of type [" + output.getClass().getName() + "]");
        } else if (output != null && dateTimeFormatter != null) {
            return formatDateOutput(output);
        } else if (output != null && numberFormat != null) {
            return formatNumberObject(output);
        }
        return output; // default
    }

    /**
     * Format a date
     * 
     * @param output the date
     * @return formatted result
     */
    private Object formatDateOutput(Object output) {
        // TODO: If the date format is the default date format, do not format ? Change in model ?
        // TODO: Add java.time check here before TemporalAccessor

        if (TemporalAccessor.class.isInstance(output)) {
            TemporalAccessor temporalAccessor = (TemporalAccessor) output;
            return dateTimeFormatter.format(temporalAccessor);
        } else if (java.sql.Date.class.isInstance(output)) {
            java.sql.Date date = ((java.sql.Date) output);
            TemporalAccessor temporalAccessor = date.toLocalDate();
            return dateTimeFormatter.format(temporalAccessor);
        } else if (java.sql.Timestamp.class.isInstance(output)) {
            java.sql.Timestamp date = ((java.sql.Timestamp) output);
            TemporalAccessor temporalAccessor = date.toLocalDateTime();
            return dateTimeFormatter.format(temporalAccessor);
        } else if (Date.class.isInstance(output)) {
            Date date = (Date) output;
            LocalDateTime localDateTime = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            return formatDateOutput(localDateTime);
        } else {
            return output;
        }
    }

    /**
     * Format a number
     * 
     * @param output the number
     * @return formatted result
     */
    private Object formatNumberObject(Object output) {
        if (Number.class.isInstance(output)) {
            Number number = (Number) output;
            return numberFormat.format(number);
        } else {
            return output;
        }
    }

    private DateTimeFormatter getDateFormat(TransformInfo formatter) {
        if (formatter != null) {
            String format = formatter.getFormat();
            String locale = formatter.getLocale();
            if (format == null) {
                return null;
            } else if (locale == null) {
                return DateTimeFormatter.ofPattern(format);
            } else {
                return DateTimeFormatter.ofPattern(format).withLocale(Locale.forLanguageTag(locale));
            }
        }
        return null;
    }

    private NumberFormat getNumberFormat(TransformInfo formatter) {
        if (formatter != null) {
            String format = formatter.getFormat();
            String locale = formatter.getLocale();

            if (format == null && locale == null) {
                return null;
            } else if (format == null) {
                return NumberFormat.getInstance(Locale.forLanguageTag(locale));
            } else if (locale == null) {
                return new DecimalFormat(format);
            } else {
                return new DecimalFormat(format,
                        DecimalFormatSymbols.getInstance(Locale.forLanguageTag(locale)));
            }
        }
        return null;
    }
}
