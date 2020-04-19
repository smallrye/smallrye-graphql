package io.smallrye.graphql.transformation;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Optional;

/**
 * Help with date creation.
 * This is not pretty. But it works.
 * 
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class DateTransformer {

    private final Optional<DateTimeFormatter> dateTimeFormatter;

    /**
     * Get a DateTransformer that will use the default values
     * 
     * @return instance of DateTransformer
     */
    public static DateTransformer transformer() {
        return new DateTransformer(Optional.empty());
    }

    /**
     * Get a DateTransformer
     * 
     * @param dateTimeFormatter the formatter to use
     * @return instance of DateTransformer
     */
    public static DateTransformer transformer(DateTimeFormatter dateTimeFormatter) {
        return new DateTransformer(Optional.ofNullable(dateTimeFormatter));
    }

    private DateTransformer(Optional<DateTimeFormatter> dateTimeFormatter) {
        this.dateTimeFormatter = dateTimeFormatter;
    }

    /**
     * Create Strings from a Date Type
     * 
     * @param dateType the date type
     * @return string representation of this date type
     */
    public String dateTypeToString(Object dateType) {

        // Date
        if (LocalDate.class.isInstance(dateType)) {
            LocalDate casted = LocalDate.class.cast(dateType);
            if (dateTimeFormatter.isPresent()) {
                return casted.format(dateTimeFormatter.get());
            } else {
                return casted.toString();
            }
        } else if (java.sql.Date.class.isInstance(dateType)) {
            java.sql.Date casted = java.sql.Date.class.cast(dateType);
            return dateTypeToString(casted.toLocalDate());

            // Time
        } else if (LocalTime.class.isInstance(dateType)) {
            LocalTime casted = LocalTime.class.cast(dateType);
            if (dateTimeFormatter.isPresent()) {
                return casted.format(dateTimeFormatter.get());
            } else {
                return casted.toString();
            }
        } else if (OffsetTime.class.isInstance(dateType)) {
            OffsetTime casted = OffsetTime.class.cast(dateType);
            if (dateTimeFormatter.isPresent()) {
                return casted.format(dateTimeFormatter.get());
            } else {
                return casted.toString();
            }
        } else if (java.sql.Time.class.isInstance(dateType)) {
            java.sql.Time casted = java.sql.Time.class.cast(dateType);
            return dateTypeToString(casted.toLocalTime());

            // DateTime
        } else if (LocalDateTime.class.isInstance(dateType)) {
            LocalDateTime casted = LocalDateTime.class.cast(dateType);
            if (dateTimeFormatter.isPresent()) {
                return casted.format(dateTimeFormatter.get());
            } else {
                return casted.toString();
            }
        } else if (OffsetDateTime.class.isInstance(dateType)) {
            OffsetDateTime casted = OffsetDateTime.class.cast(dateType);
            if (dateTimeFormatter.isPresent()) {
                return casted.format(dateTimeFormatter.get());
            } else {
                return casted.toString();
            }
        } else if (ZonedDateTime.class.isInstance(dateType)) {
            ZonedDateTime casted = ZonedDateTime.class.cast(dateType);
            if (dateTimeFormatter.isPresent()) {
                return casted.format(dateTimeFormatter.get());
            } else {
                return casted.toString();
            }
        } else if (java.sql.Timestamp.class.isInstance(dateType)) {
            java.sql.Timestamp casted = java.sql.Timestamp.class.cast(dateType);
            return dateTypeToString(casted.toLocalDateTime());
        } else if (Date.class.isInstance(dateType)) {
            Date casted = Date.class.cast(dateType);
            return dateTypeToString(casted.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        }
        // default
        return dateType.toString();
    }

    /**
     * Create Dates from a String
     * 
     * @param input the date string
     * @param typeClassName the date type name
     * @return some date type object, maybe
     */
    public Optional<Object> stringToDateType(String input, String typeClassName) throws DateTimeException {

        // Date
        if (typeClassName.equals(LocalDate.class.getName())) {
            if (dateTimeFormatter.isPresent()) {
                return Optional.of(LocalDate.parse(input, dateTimeFormatter.get()));
            } else {
                return Optional.of(LocalDate.parse(input));
            }
        } else if (typeClassName.equals(java.sql.Date.class.getName())) {
            if (dateTimeFormatter.isPresent()) {
                LocalDate localdate = LocalDate.parse(input, dateTimeFormatter.get());
                return Optional.of(java.sql.Date.valueOf(localdate));
            } else {
                LocalDate localdate = LocalDate.parse(input);
                return Optional.of(java.sql.Date.valueOf(localdate));
            }

            // Time    
        } else if (typeClassName.equals(LocalTime.class.getName())) {
            if (dateTimeFormatter.isPresent()) {
                return Optional.of(LocalTime.parse(input, dateTimeFormatter.get()));
            } else {
                return Optional.of(LocalTime.parse(input));
            }
        } else if (typeClassName.equals(java.sql.Time.class.getName())) {
            if (dateTimeFormatter.isPresent()) {
                LocalTime localtime = LocalTime.parse(input, dateTimeFormatter.get());
                return Optional.of(java.sql.Time.valueOf(localtime));
            } else {
                LocalTime localtime = LocalTime.parse(input);
                return Optional.of(java.sql.Time.valueOf(localtime));
            }
        } else if (typeClassName.equals(OffsetTime.class.getName())) {
            if (dateTimeFormatter.isPresent()) {
                return Optional.of(OffsetTime.parse(input, dateTimeFormatter.get()));
            } else {
                return Optional.of(OffsetTime.parse(input));
            }

            // DateTime    
        } else if (typeClassName.equals(LocalDateTime.class.getName())) {
            if (dateTimeFormatter.isPresent()) {
                return Optional.of(LocalDateTime.parse(input, dateTimeFormatter.get()));
            } else {
                return Optional.of(LocalDateTime.parse(input));
            }
        } else if (typeClassName.equals(java.sql.Timestamp.class.getName())) {
            if (dateTimeFormatter.isPresent()) {
                LocalDateTime localdatetime = LocalDateTime.parse(input, dateTimeFormatter.get());
                return Optional.of(java.sql.Timestamp.valueOf(localdatetime));
            } else {
                LocalDateTime localdatetime = LocalDateTime.parse(input);
                return Optional.of(java.sql.Timestamp.valueOf(localdatetime));
            }
        } else if (typeClassName.equals(Date.class.getName())) {
            if (dateTimeFormatter.isPresent()) {
                LocalDateTime localdatetime = LocalDateTime.parse(input, dateTimeFormatter.get());
                return Optional.of(java.util.Date.from(localdatetime.atZone(ZoneId.systemDefault()).toInstant()));
            } else {
                LocalDateTime localdatetime = LocalDateTime.parse(input);
                return Optional.of(java.util.Date.from(localdatetime.atZone(ZoneId.systemDefault()).toInstant()));
            }
        } else if (typeClassName.equals(ZonedDateTime.class.getName())) {
            if (dateTimeFormatter.isPresent()) {
                return Optional.of(ZonedDateTime.parse(input, dateTimeFormatter.get()));
            } else {
                return Optional.of(ZonedDateTime.parse(input));
            }
        } else if (typeClassName.equals(OffsetDateTime.class.getName())) {
            if (dateTimeFormatter.isPresent()) {
                return Optional.of(OffsetDateTime.parse(input, dateTimeFormatter.get()));
            } else {
                return Optional.of(OffsetDateTime.parse(input));
            }
        } else {

            // Not a date
            return Optional.empty();
        }
    }

}
