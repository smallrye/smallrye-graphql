package io.smallrye.graphql.scalar.custom;

import static io.smallrye.graphql.SmallRyeGraphQLServerMessages.msg;

import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;

import graphql.language.IntValue;
import graphql.language.Value;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import io.smallrye.graphql.spi.ClassloadingService;

public class IntCoercing implements Coercing<Object, BigInteger> {

    private final Class<? extends CustomIntScalar> customScalarClass;

    public IntCoercing(String customScalarClass) {
        ClassloadingService classloadingService = ClassloadingService.get();
        this.customScalarClass = (Class<? extends CustomIntScalar>) classloadingService.loadClass(customScalarClass);
    }

    private CustomIntScalar newInstance(BigInteger graphqlPrimitiveValue)
            throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        return graphqlPrimitiveValue == null ? null
                : customScalarClass.getConstructor(BigInteger.class).newInstance(graphqlPrimitiveValue);
    }

    /* Coercing implementation. Forgive the deprecated methods. */
    private static String typeName(Object input) {
        if (input == null) {
            return "null";
        }
        return input.getClass().getSimpleName();
    }

    private CustomIntScalar convertImpl(Object input)
            throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        if (input instanceof BigInteger) {
            return newInstance((BigInteger) input);
        } else if (input.getClass().isAssignableFrom(customScalarClass)) {
            return (CustomIntScalar) input;
        }
        throw new RuntimeException("Unable to convert null input.");
    }

    @Override
    public BigInteger serialize(Object input) throws CoercingSerializeException {
        CustomIntScalar result;
        try {
            result = convertImpl(input);
        } catch (InvocationTargetException | IllegalAccessException | InstantiationException | NoSuchMethodException e) {
            throw new CoercingSerializeException("Unable to serialize input: " + input, e);
        } catch (RuntimeException e) {
            throw msg.coercingSerializeException("BigInteger or class extending " +
                    customScalarClass, typeName(input), e);
        }
        return result.intValueForSerialization();
    }

    @Override
    public Object parseValue(Object input) throws CoercingParseValueException {
        Object result;
        try {
            result = convertImpl(input);
        } catch (InvocationTargetException | IllegalAccessException | InstantiationException | NoSuchMethodException e) {
            throw new CoercingParseValueException("Unable to serialize input: " + input, e);
        } catch (RuntimeException e) {
            throw msg.coercingParseValueException("BigInteger or class extending " +
                    customScalarClass, typeName(input), null);
        }
        return result;
    }

    @Override
    public Object parseLiteral(Object input) throws CoercingParseLiteralException {
        if (!(input instanceof IntValue)) {
            throw new CoercingParseLiteralException(
                    "Expected a String AST type object but was '" + typeName(input) + "'.");
        }
        try {
            return newInstance(((IntValue) input).getValue());
        } catch (InvocationTargetException | IllegalAccessException | InstantiationException | NoSuchMethodException e) {
            throw new CoercingParseLiteralException("Unable to parse literal: " + input, e);
        }
    }

    @Override
    public Value<?> valueToLiteral(Object input) {
        BigInteger s = serialize(input);
        return IntValue.newIntValue(s).build();
    }

}
