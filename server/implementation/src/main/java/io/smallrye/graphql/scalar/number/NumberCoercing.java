package io.smallrye.graphql.scalar.number;

import static io.smallrye.graphql.SmallRyeGraphQLServerMessages.msg;

import java.math.BigDecimal;
import java.math.BigInteger;

import graphql.language.FloatValue;
import graphql.language.IntValue;
import graphql.language.StringValue;
import graphql.schema.Coercing;

/**
 * The Coercing used by numbers
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class NumberCoercing implements Coercing {

    private final Class[] supportedTypes;
    private final Converter converter;
    private final String name;

    public NumberCoercing(String name, Converter converter, Class... supportedTypes) {
        this.name = name;
        this.converter = converter;
        this.supportedTypes = supportedTypes;
    }

    private Object convertImpl(Object input) {

        for (Class supportedClass : supportedTypes) {
            if (supportedClass.isInstance(input)) {
                return supportedClass.cast(input);
            }
        }

        if (input instanceof Number) {
            return input;
        } else if (input instanceof String) {
            return input;
        } else {
            throw msg.numberFormatException(input.toString());
        }

    }

    @Override
    public Object serialize(Object input) {
        if (input == null)
            return null;
        try {
            return convertImpl(input);
        } catch (NumberFormatException e) {
            throw msg.coercingSerializeException(name, input.getClass().getSimpleName(), e);
        }
    }

    @Override
    public Object parseValue(Object input) {
        try {
            return convertImpl(input);
        } catch (NumberFormatException e) {
            throw msg.coercingParseValueException(name, input.getClass().getSimpleName(), e);
        }
    }

    @Override
    public Object parseLiteral(Object input) {
        if (input == null)
            return null;
        if (input instanceof StringValue) {
            try {
                BigDecimal value = new BigDecimal(((StringValue) input).getValue());
                return converter.fromBigDecimal(value);
            } catch (NumberFormatException e) {
                // TODO: Do we still need this ? Here we allow strings through becauce of Numberformatting.
                return ((StringValue) input).getValue();
            }

        } else if (input instanceof IntValue) {
            BigInteger value = ((IntValue) input).getValue();
            if (!converter.isInRange(value)) {
                throw msg.coercingParseLiteralException(name, value.toString());
            }
            return converter.fromBigInteger(value);
        } else if (input instanceof FloatValue) {
            BigDecimal value = ((FloatValue) input).getValue();
            return converter.fromBigDecimal(value);
        } else if (input instanceof BigDecimal) {
            BigDecimal value = (BigDecimal) input;
            return converter.fromBigDecimal(value);
        } else if (input instanceof BigInteger) {
            BigInteger value = (BigInteger) input;
            if (!converter.isInRange(value)) {
                throw msg.coercingParseLiteralException(name, value.toString());
            }
            return converter.fromBigInteger(value);
        }
        throw msg.coercingParseLiteralException(input.getClass().getSimpleName());
    }
}
