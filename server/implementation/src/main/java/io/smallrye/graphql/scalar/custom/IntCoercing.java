package io.smallrye.graphql.scalar.custom;

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

    private CustomIntScalar newInstance(BigInteger graphqlPrimitiveValue) {
        try {
            return graphqlPrimitiveValue == null ? null
                    : customScalarClass.getConstructor(BigInteger.class).newInstance(graphqlPrimitiveValue);
        } catch (Exception e) {
            throw new CoercingSerializeException("TODO bdupras better error handling here", e);
        }
    }

    /* Coercing implementation. Forgive the deprecated methods. */
    private static String typeName(Object input) {
        if (input == null) {
            return "null";
        }
        return input.getClass().getSimpleName();
    }

    private CustomIntScalar convertImpl(Object input) {
        if (input instanceof BigInteger) {
            return newInstance((BigInteger) input);
        } else if (input.getClass().isAssignableFrom(customScalarClass)) {
            return (CustomIntScalar) input;
        }
        return null;
    }

    @Override
    public BigInteger serialize(Object input) throws CoercingSerializeException {
        CustomIntScalar result = convertImpl(input);
        if (result == null) {
            throw new CoercingSerializeException(
                    "Expected type String but was '" + typeName(input) + "'.");
        }
        return result.intValue();
    }

    @Override
    public Object parseValue(Object input) throws CoercingParseValueException {
        Object result = convertImpl(input);
        if (result == null) {
            throw new CoercingParseValueException(
                    "Expected type String but was '" + typeName(input) + "'.");
        }
        return result;
    }

    @Override
    public Object parseLiteral(Object input) throws CoercingParseLiteralException {
        if (!(input instanceof IntValue)) {
            throw new CoercingParseLiteralException(
                    "Expected a String AST type object but was '" + typeName(input) + "'.");
        }
        return newInstance(((IntValue) input).getValue());
    }

    @Override
    public Value<?> valueToLiteral(Object input) {
        BigInteger s = serialize(input);
        return IntValue.newIntValue(s).build();
    }

}
