package io.smallrye.graphql.scalar.time;

import java.time.DateTimeException;

import graphql.language.StringValue;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;

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
            throw new DateTimeException("" + input);
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
            throw new CoercingSerializeException(
                    "Expected type '" + name + "' but was '" + input.getClass().getSimpleName() + "'.", e);
        }
    }

    @Override
    public Object parseValue(Object input) {
        try {
            return convertImpl(input);
        } catch (DateTimeException e) {
            throw new CoercingParseValueException(
                    "Expected type '" + name + "' but was '" + input.getClass().getSimpleName() + "'.");
        }
    }

    @Override
    public Object parseLiteral(Object input) {
        if (input == null)
            return null;

        if (input instanceof StringValue) {
            // We need to get a String value of this date
            String value = ((StringValue) input).getValue();

            //return converter.fromString(value); // We can only do this if we make all transformations String scalars
            return value;
        } else {
            throw new CoercingParseLiteralException(
                    "Expected AST type 'StringValue' but was '" + input.getClass().getSimpleName() + "'.");
        }

    }
}
