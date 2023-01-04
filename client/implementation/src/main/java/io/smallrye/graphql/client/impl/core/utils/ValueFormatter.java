package io.smallrye.graphql.client.impl.core.utils;

import java.lang.reflect.Array;
import java.time.LocalDate;
import java.util.UUID;

import io.smallrye.graphql.client.core.exceptions.BuildException;
import io.smallrye.graphql.client.impl.core.EnumImpl;
import io.smallrye.graphql.client.impl.core.InputObjectImpl;
import io.smallrye.graphql.client.impl.core.VariableImpl;

public class ValueFormatter {

    private final static Class<?>[] QUOTED_VALUES = new Class[] { String.class, Character.class, LocalDate.class, UUID.class };
    private final static Class<?>[] UNQUOTED_VALUES = new Class[] { Number.class, Boolean.class };

    public static boolean assignableFrom(Class<?> clazz, Class<?>[] candidates) {
        for (Class<?> candidate : candidates) {
            if (candidate.isAssignableFrom(clazz)) {
                return true;
            }
        }
        return false;
    }

    public static String format(Object value) throws BuildException {
        if (value == null) {
            return "null";
        } else if (value instanceof VariableImpl) {
            VariableImpl var = (VariableImpl) value;
            return "$" + var.getName();
        } else if (value instanceof InputObjectImpl) {
            InputObjectImpl inputObject = (InputObjectImpl) value;
            return inputObject.build();
        } else if (value instanceof EnumImpl) {
            EnumImpl gqlEnum = (EnumImpl) value;
            return gqlEnum.build();
        } else if (value.getClass().isArray()) {
            return _processArray(value);
        } else if (value instanceof Iterable) {
            return _processIterable((Iterable<?>) value);
        } else {
            if (assignableFrom(value.getClass(), QUOTED_VALUES)) {
                return _getAsQuotedString(String.valueOf(value));
            } else if (assignableFrom(value.getClass(), UNQUOTED_VALUES)) {
                return value.toString();
            }
            throw new IllegalStateException("Could not format " + value.getClass() + ": Unsupported type.");
        }
    }

    private static String _processIterable(Iterable<?> iterable) throws BuildException {
        StringBuilder builder = new StringBuilder();

        boolean first = true;
        builder.append("[");
        for (Object v : iterable) {
            if (first) {
                first = false;
            } else {
                builder.append(",");
            }
            builder.append(format(v));
        }
        builder.append("]");

        return builder.toString();
    }

    private static String _processArray(Object array) throws BuildException {
        StringBuilder builder = new StringBuilder();

        int length = Array.getLength(array);
        builder.append("[");
        for (int i = 0; i < length; i++) {
            builder.append(format(Array.get(array, i)));
            if (i < length - 1) {
                builder.append(",");
            }
        }
        builder.append("]");

        return builder.toString();
    }

    private static String _getAsQuotedString(String value) {
        StringBuilder builder = new StringBuilder();

        builder.append('"');
        for (char c : value.toCharArray()) {
            switch (c) {
                case '"':
                case '\\':
                    builder.append('\\');
                    builder.append(c);
                    break;
                case '\r':
                    builder.append("\\r");
                    break;
                case '\n':
                    builder.append("\\n");
                    break;
                default:
                    if (c < 0x20) {
                        builder.append(String.format("\\u%04x", (int) c));
                    } else {
                        builder.append(c);
                    }
                    break;
            }
        }
        builder.append('"');

        return builder.toString();
    }
}
