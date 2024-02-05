package io.smallrye.graphql.scalar.federation;

import static io.smallrye.graphql.SmallRyeGraphQLServerMessages.msg;

import java.util.Map;

import graphql.language.StringValue;
import graphql.language.Value;
import graphql.schema.Coercing;
import io.smallrye.graphql.api.federation.FieldSet;

public class FieldSetCoercing implements Coercing<Object, String> {

    private static String typeName(Object input) {
        if (input == null) {
            return "null";
        }
        return input.getClass().getSimpleName();
    }

    private String convertImpl(Object input) {
        if (input instanceof Map) {
            Object value = ((Map<?, ?>) input).get("value");
            if (value instanceof String) {
                return (String) value;
            } else {
                throw new RuntimeException("Can not parse a String from [" + typeName(value) + "]");
            }
        } else {
            throw new RuntimeException("Can not parse a FieldSet from [" + typeName(input) + "]");
        }
    }

    @Override
    public String serialize(Object input) {
        if (input == null)
            return null;
        try {
            return convertImpl(input);
        } catch (RuntimeException e) {
            throw msg.coercingSerializeException(FieldSet.class.getSimpleName(), typeName(input), e);
        }
    }

    @Override
    public Object parseValue(Object input) {
        try {
            return convertImpl(input);
        } catch (RuntimeException e) {
            throw msg.coercingParseValueException(FieldSet.class.getSimpleName(), typeName(input), e);
        }
    }

    @Override
    public Object parseLiteral(Object input) {
        if (input == null)
            return null;

        if (input instanceof StringValue) {
            return ((StringValue) input).getValue();
        } else {
            throw msg.coercingParseLiteralException(typeName(input));
        }
    }

    @Override
    public Value<?> valueToLiteral(Object input) {
        String s = serialize(input);
        return StringValue.newStringValue(s).build();
    }

}
