package io.smallrye.graphql.scalar.custom;

import java.math.BigDecimal;

import graphql.language.FloatValue;
import graphql.language.Value;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import io.smallrye.graphql.spi.ClassloadingService;

public class FloatCoercing implements Coercing<Object, BigDecimal> {

    private final Class<? extends CustomFloatScalar> customScalarClass;

    public FloatCoercing(String customScalarClass) {
        ClassloadingService classloadingService = ClassloadingService.get();
        this.customScalarClass = (Class<? extends CustomFloatScalar>) classloadingService.loadClass(customScalarClass);
    }

    private CustomFloatScalar newInstance(BigDecimal graphqlPrimitiveValue) {
        try {
            return graphqlPrimitiveValue == null ? null
                    : customScalarClass.getConstructor(BigDecimal.class).newInstance(graphqlPrimitiveValue);
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

    private CustomFloatScalar convertImpl(Object input) {
        if (input instanceof BigDecimal) {
            return newInstance((BigDecimal) input);
        } else if (input.getClass().isAssignableFrom(customScalarClass)) {
            return (CustomFloatScalar) input;
        }
        return null;
    }

    @Override
    public BigDecimal serialize(Object input) throws CoercingSerializeException {
        CustomFloatScalar result = convertImpl(input);
        if (result == null) {
            throw new CoercingSerializeException(
                    "Expected type String but was '" + typeName(input) + "'.");
        }
        return result.floatValueForSerialization();
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
        if (!(input instanceof FloatValue)) {
            throw new CoercingParseLiteralException(
                    "Expected a String AST type object but was '" + typeName(input) + "'.");
        }
        return newInstance(((FloatValue) input).getValue());
    }

    @Override
    public Value<?> valueToLiteral(Object input) {
        BigDecimal s = serialize(input);
        return FloatValue.newFloatValue(s).build();
    }

}
