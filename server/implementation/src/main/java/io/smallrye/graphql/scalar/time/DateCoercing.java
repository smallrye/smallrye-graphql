package io.smallrye.graphql.scalar.time;

import static io.smallrye.graphql.SmallRyeGraphQLServerMessages.msg;

import java.time.DateTimeException;

import graphql.language.StringValue;
import graphql.schema.Coercing;

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
}
