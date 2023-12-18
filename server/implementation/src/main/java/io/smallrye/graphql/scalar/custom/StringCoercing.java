package io.smallrye.graphql.scalar.custom;

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

    private CustomStringScalar newInstance(String graphqlPrimitiveValue) {
        try {
            return graphqlPrimitiveValue == null ? null
                    : customScalarClass.getConstructor(String.class).newInstance(graphqlPrimitiveValue);
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

    private CustomStringScalar convertImpl(Object input) {
        if (input instanceof String) {
            return newInstance((String) input);
        } else if (input.getClass().isAssignableFrom(customScalarClass)) {
            return (CustomStringScalar) input;
        }
        return null;
    }

    @Override
    public String serialize(Object input) throws CoercingSerializeException {
        CustomStringScalar result = convertImpl(input);
        if (result == null) {
            throw new CoercingSerializeException(
                    "Expected type String but was '" + typeName(input) + "'.");
        }
        return result.stringValueForSerialization();
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
        if (!(input instanceof StringValue)) {
            throw new CoercingParseLiteralException(
                    "Expected a String AST type object but was '" + typeName(input) + "'.");
        }
        return newInstance(((StringValue) input).getValue());
    }

    @Override
    public Value<?> valueToLiteral(Object input) {
        String s = serialize(input);
        return StringValue.newStringValue(s).build();
    }

}
