package io.smallrye.graphql.scalar.custom;

import static io.smallrye.graphql.SmallRyeGraphQLServerMessages.msg;

import java.lang.reflect.InvocationTargetException;

import graphql.language.StringValue;
import graphql.language.Value;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import io.smallrye.graphql.spi.ClassloadingService;

public class StringCoercing implements Coercing<Object, String> {

    private final Class<? extends CustomStringScalar> customScalarClass;

    public StringCoercing(String customScalarClass) {
        ClassloadingService classloadingService = ClassloadingService.get();
        this.customScalarClass = (Class<? extends CustomStringScalar>) classloadingService.loadClass(customScalarClass);
    }

    private CustomStringScalar newInstance(String graphqlPrimitiveValue)
            throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {

        return graphqlPrimitiveValue == null ? null
                : customScalarClass.getConstructor(String.class).newInstance(graphqlPrimitiveValue);

    }

    /* Coercing implementation. Forgive the deprecated methods. */
    private static String typeName(Object input) {
        if (input == null) {
            return "null";
        }
        return input.getClass().getSimpleName();
    }

    private CustomStringScalar convertImpl(Object input)
            throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        if (input instanceof String) {
            return newInstance((String) input);
        } else if (input.getClass().isAssignableFrom(customScalarClass)) {
            return (CustomStringScalar) input;
        }
        throw new RuntimeException("Unable to convert null input.");
    }

    @Override
    public String serialize(Object input) throws CoercingSerializeException {
        CustomStringScalar result;
        try {
            result = convertImpl(input);
        } catch (InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
            throw new CoercingSerializeException("Unable to serialize object.", e);
        } catch (RuntimeException e) {
            throw msg.coercingSerializeException("String or class extending " +
                    customScalarClass, typeName(input), null);
        }
        return result.stringValueForSerialization();
    }

    @Override
    public Object parseValue(Object input) throws CoercingParseValueException {
        Object result;
        try {
            result = convertImpl(input);
        } catch (InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
            throw new CoercingParseValueException("Unable to parse value: " + input, e);
        } catch (RuntimeException e) {
            throw msg.coercingParseValueException("String or class extending " +
                    customScalarClass, typeName(input), null);
        }
        return result;
    }

    @Override
    public Object parseLiteral(Object input) throws CoercingParseLiteralException {
        if (!(input instanceof StringValue)) {
            throw new CoercingParseLiteralException(
                    "Expected a String AST type object but was '" + typeName(input) + "'.");
        }
        try {
            return newInstance(((StringValue) input).getValue());
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
            throw new CoercingParseLiteralException("Unable to parse literal: " + input, e);
        }
    }

    @Override
    public Value<?> valueToLiteral(Object input) {
        String s = serialize(input);
        return StringValue.newStringValue(s).build();
    }

}
