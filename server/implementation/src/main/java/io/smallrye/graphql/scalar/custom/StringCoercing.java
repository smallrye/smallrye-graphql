package io.smallrye.graphql.scalar.custom;

import graphql.language.StringValue;
import graphql.language.Value;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import io.smallrye.graphql.spi.ClassloadingService;

/**
 * TODO bdupras - implement the non-deprecated methods of Coercing
 */
public class StringCoercing implements Coercing<Object, String> {
    private final ClassloadingService classloadingService = ClassloadingService.get();
    private final Class<?> customScalarClass;

    public StringCoercing(String customScalarClass) {
        this.customScalarClass = classloadingService.loadClass(customScalarClass);
    }

    private Object newInstance(String graphqlPrimitiveValue) {
        try {
            return graphqlPrimitiveValue == null ? null
                    : customScalarClass.getConstructor(String.class).newInstance(graphqlPrimitiveValue);
        } catch (Exception e) {
            throw new RuntimeException("TODO bdupras better error handling here", e);
        }
    }

    /* Coercing implementation. Forgive the deprecated methods. */
    private static String typeName(Object input) {
        if (input == null) {
            return "null";
        }
        return input.getClass().getSimpleName();
    }

    private Object convertImpl(Object input) {
        if (input instanceof String) {
            try {
                return newInstance((String) input);
            } catch (IllegalArgumentException ex) {
                return null;
            }
        } else if (input.getClass().isAssignableFrom(customScalarClass)) {
            return input;
        }
        return null;
    }

    @Override
    public String serialize(Object input) throws CoercingSerializeException {
        Object result = convertImpl(input);
        if (result == null) {
            throw new CoercingSerializeException(
                    "Expected type String but was '" + typeName(input) + "'.");
        }
        return result.toString();
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
        try {
            return newInstance(((StringValue) input).getValue());
        } catch (IllegalArgumentException ex) {
            throw new CoercingParseLiteralException(
                    "Expected something that we can convert to a CustomStringScalar but was invalid");
        }
    }

    @Override
    public Value<?> valueToLiteral(Object input) {
        String s = serialize(input);
        return StringValue.newStringValue(s).build();
    }

}
