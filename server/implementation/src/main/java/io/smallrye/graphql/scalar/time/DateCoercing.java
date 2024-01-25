package io.smallrye.graphql.scalar.time;

import static io.smallrye.graphql.SmallRyeGraphQLServerMessages.msg;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import graphql.language.StringValue;
import graphql.language.Value;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;

/**
 * The Coercing used by dates
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class DateCoercing implements Coercing {

    private final Class[] supportedTypes;
    private final String name;

    public DateCoercing(String name, Class... supportedTypes) {
        this.name = name;
        this.supportedTypes = supportedTypes;
    }

    private Object convertImpl(Object input) {
        for (Class supportedType : supportedTypes) {
            if (supportedType.isInstance(input)) {
                return supportedType.cast(input);
            }
        }

        if (input instanceof String) {
            return input;
        } else {
            throw msg.unknownDateFormat(input.toString());
        }
    }

    // Get's called on startup for @DefaultValue
    @Override
    public Object serialize(Object input) {
        if (input == null)
            return null;
        try {
            return convertImpl(input);
        } catch (DateTimeException e) {
            throw msg.coercingSerializeException(name, input.getClass().getSimpleName(), e);
        }
    }

    @Override
    public Object parseValue(Object input) {
        try {
            return convertImpl(input);
        } catch (DateTimeException e) {
            throw msg.coercingParseValueException(name, input.getClass().getSimpleName(), e);
        }
    }

    @Override
    public Object parseLiteral(Object input) {
        if (input == null)
            return null;

        if (input instanceof StringValue) {
            // We need to get a String value of this date
            return ((StringValue) input).getValue();
        } else {
            throw msg.coercingParseLiteralException(input.getClass().getSimpleName());
        }
    }

    @Override
    public Value<?> valueToLiteral(Object input) {
        Object s = serialize(input);
        String formattedDate;
        if (s instanceof String) {
            return StringValue.newStringValue((String) s).build();
        } else if (s instanceof LocalDate) {
            formattedDate = DateTimeFormatter.ISO_LOCAL_DATE.format((LocalDate) s);
        } else if (s instanceof LocalTime) {
            formattedDate = DateTimeFormatter.ISO_LOCAL_TIME.format((LocalTime) s);
        } else if (s instanceof LocalDateTime) {
            formattedDate = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format((LocalDateTime) s);
        } else if (s instanceof OffsetTime) {
            formattedDate = DateTimeFormatter.ISO_OFFSET_TIME.format((OffsetTime) s);
        } else if (s instanceof OffsetDateTime) {
            formattedDate = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format((OffsetDateTime) s);
        } else if (s instanceof ZonedDateTime) {
            formattedDate = DateTimeFormatter.ISO_ZONED_DATE_TIME.format((ZonedDateTime) s);
        } else if (s instanceof Instant) {
            formattedDate = DateTimeFormatter.ISO_INSTANT.format((Instant) s);
        } else {
            throw new CoercingParseLiteralException("Input is not a valid date type: " + input);
        }
        return new StringValue(formattedDate);
    }
}
