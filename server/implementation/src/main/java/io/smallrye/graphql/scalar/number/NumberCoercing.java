package io.smallrye.graphql.scalar.number;

import static io.smallrye.graphql.SmallRyeGraphQLServerMessages.msg;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.OptionalDouble;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import graphql.Scalars;
import graphql.language.FloatValue;
import graphql.language.IntValue;
import graphql.language.StringValue;
import graphql.language.Value;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import io.smallrye.graphql.transformation.NumberTransformer;

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

        if (input instanceof BigInteger value) {
            return converter.fromBigInteger(value);
        } else if (input instanceof BigDecimal value) {
            return converter.fromBigDecimal(value);
        } else if (input instanceof Float value) {
            NumberTransformer.finiteFloat(value);
        } else if (input instanceof Double value) {
            NumberTransformer.finiteDouble(value);
        } else if (input instanceof OptionalDouble optional && optional.isPresent()) {
            NumberTransformer.finiteDouble(optional.getAsDouble());
        }

        for (Class supportedClass : supportedTypes) {
            if (supportedClass.isInstance(input)) {
                return supportedClass.cast(input);
            }
        }

        if (input instanceof Byte || input instanceof Short || input instanceof Integer || input instanceof Long
                || input instanceof AtomicInteger || input instanceof AtomicLong) {
            return converter.fromLong(((Number) input).longValue());
        } else if (input instanceof Float value) {
            return converter.fromFloat(value);
        } else if (input instanceof Double value) {
            return converter.fromDouble(value);
        } else if (input instanceof Number || input instanceof String) {
            return converter.fromBigDecimal(new BigDecimal(input.toString()));
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
        } catch (NumberFormatException | ArithmeticException e) {
            throw msg.coercingSerializeException(name, input.getClass().getSimpleName(), e);
        }
    }

    @Override
    public Object parseValue(Object input) {
        try {
            return convertImpl(input);
        } catch (NumberFormatException | ArithmeticException e) {
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
                return parseDecimalLiteral(value, input);
            } catch (NumberFormatException e) {
                throw msg.numberCoercingParseException(input.toString());
            }
        } else if (input instanceof IntValue) {
            BigInteger value = ((IntValue) input).getValue();
            try {
                return converter.fromBigInteger(value);
            } catch (ArithmeticException e) {
                throw msg.coercingParseLiteralException(name, input.toString());
            }
        } else if (input instanceof FloatValue) {
            return parseDecimalLiteral(((FloatValue) input).getValue(), input);
        } else if (input instanceof BigDecimal) {
            return parseDecimalLiteral((BigDecimal) input, input);
        } else if (input instanceof BigInteger) {
            BigInteger value = (BigInteger) input;
            try {
                return converter.fromBigInteger(value);
            } catch (ArithmeticException e) {
                throw msg.coercingParseLiteralException(name, input.toString());
            }
        }
        throw msg.coercingParseLiteralException(input.getClass().getSimpleName());
    }

    private Object parseDecimalLiteral(BigDecimal value, Object input) {
        try {
            return converter.fromBigDecimal(value);
        } catch (ArithmeticException e) {
            if (Scalars.GraphQLInt.getName().equals(name)
                    && value.compareTo(BigDecimal.valueOf(Integer.MIN_VALUE)) >= 0
                    && value.compareTo(BigDecimal.valueOf(Integer.MAX_VALUE)) <= 0) {
                // An in-range value can only fail the exact Int conversion if it has a fractional part.
                throw msg.integerCoercingParseException(input.toString());
            }
            throw msg.coercingParseLiteralException(name, input.toString());
        }
    }

    @Override
    public Value<?> valueToLiteral(Object input) {
        Object serialized = serialize(input);
        if (serialized instanceof BigDecimal) {
            return FloatValue.newFloatValue((BigDecimal) serialized).build();
        } else if (serialized instanceof BigInteger) {
            return IntValue.newIntValue((BigInteger) serialized).build();
        } else if (serialized instanceof Float || serialized instanceof Double) {
            return new FloatValue(BigDecimal.valueOf(((Number) serialized).doubleValue()));
        } else if (serialized instanceof Number) {
            // For other Number types like Integer, Long, etc.
            return new IntValue(BigInteger.valueOf(((Number) serialized).longValue()));
        } else {
            throw new CoercingParseLiteralException("Input is not a valid number type: " + input);
        }
    }
}
