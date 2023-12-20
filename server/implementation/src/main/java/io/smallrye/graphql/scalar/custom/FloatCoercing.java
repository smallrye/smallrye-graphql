package io.smallrye.graphql.scalar.custom;

import static io.smallrye.graphql.SmallRyeGraphQLServerMessages.msg;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;

import graphql.language.FloatValue;
import graphql.language.Value;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import io.smallrye.graphql.api.CustomFloatScalar;
import io.smallrye.graphql.spi.ClassloadingService;

public class FloatCoercing implements Coercing<Object, BigDecimal> {

    private final Class<? extends CustomFloatScalar> customScalarClass;

    public FloatCoercing(String customScalarClass) {
        ClassloadingService classloadingService = ClassloadingService.get();
        this.customScalarClass = (Class<? extends CustomFloatScalar>) classloadingService.loadClass(customScalarClass);
    }

    private CustomFloatScalar newInstance(BigDecimal graphqlPrimitiveValue)
            throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        return graphqlPrimitiveValue == null ? null
                : customScalarClass.getConstructor(BigDecimal.class).newInstance(graphqlPrimitiveValue);

    }

    /* Coercing implementation. Forgive the deprecated methods. */
    private static String typeName(Object input) {
        if (input == null) {
            return "null";
        }
        return input.getClass().getSimpleName();
    }

    private CustomFloatScalar convertImpl(Object input)
            throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        if (input instanceof BigDecimal) {
            return newInstance((BigDecimal) input);
        } else if (input.getClass().isAssignableFrom(customScalarClass)) {
            return (CustomFloatScalar) input;
        }
        throw new RuntimeException("Unable to convert null input.");
    }

    @Override
    public BigDecimal serialize(Object input) throws CoercingSerializeException {
        CustomFloatScalar result;
        try {
            result = convertImpl(input);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new CoercingSerializeException("Unable to serialize input " + input, e);
        } catch (RuntimeException e) {
            throw msg.coercingSerializeException("BigDecimal or class extending " +
                    customScalarClass, typeName(input), null);
        }
        return result.floatValueForSerialization();
    }

    @Override
    public Object parseValue(Object input) throws CoercingParseValueException {
        Object result;
        try {
            result = convertImpl(input);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new CoercingParseValueException("Unable to parse input: " + input, e);
        } catch (RuntimeException e) {
            throw msg.coercingParseValueException("BigDecimal or class extending " +
                    customScalarClass, typeName(input), null);
        }
        return result;
    }

    @Override
    public Object parseLiteral(Object input) throws CoercingParseLiteralException {
        if (!(input instanceof FloatValue)) {
            throw new CoercingParseLiteralException(
                    "Expected a String AST type object but was '" + typeName(input) + "'.");
        }
        try {
            return newInstance(((FloatValue) input).getValue());
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new CoercingParseLiteralException("Unable to parse literal:" + input, e);
        }
    }

    @Override
    public Value<?> valueToLiteral(Object input) {
        BigDecimal s = serialize(input);
        return FloatValue.newFloatValue(s).build();
    }

}
