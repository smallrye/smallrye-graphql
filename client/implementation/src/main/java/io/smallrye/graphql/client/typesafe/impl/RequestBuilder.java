package io.smallrye.graphql.client.typesafe.impl;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import io.smallrye.graphql.client.typesafe.api.Header;
import io.smallrye.graphql.client.typesafe.impl.reflection.MethodInfo;
import io.smallrye.graphql.client.typesafe.impl.reflection.ParameterInfo;
import io.smallrye.graphql.client.typesafe.impl.reflection.TypeInfo;

class RequestBuilder {
    private final MethodInfo method;
    private final StringBuilder request = new StringBuilder();

    public RequestBuilder(MethodInfo method) {
        this.method = method;
    }

    String build() {
        request.append(method.getName());
        List<ParameterInfo> parameters = method.parameters()
                .filter(parameterInfo -> !parameterInfo.isAnnotated(Header.class))
                .collect(toList());
        if (parameters.size() > 0) {
            request.append("(");
            Repeated repeated = new Repeated(", ");
            for (ParameterInfo parameterInfo : parameters) {
                request.append(repeated);
                appendParam(parameterInfo);
            }
            request.append(")");
        }
        return request.toString();
    }

    private void appendParam(ParameterInfo parameter) {
        request.append(parameter.getName()).append(": ");
        buildParam(parameter.getType(), parameter.getValue());
    }

    private void buildParam(TypeInfo type, Object value) {
        if (value == null)
            request.append("null");
        else if (type.isScalar())
            buildScalarParam(type, value);
        else if (type.isCollection())
            buildArrayParam(type.getItemType(), asList(value));
        else
            buildObjectParam(type, value);
    }

    private List<?> asList(Object value) {
        if (value instanceof List)
            return (List<?>) value;
        if (value.getClass().isArray())
            return Arrays.asList((Object[]) value);
        return new ArrayList<>((Collection<?>) value);
    }

    private void buildScalarParam(TypeInfo type, Object value) {
        boolean quoted = !unquoted(type);
        if (quoted)
            request.append("\"");
        request.append(type.stringValue(value));
        if (quoted)
            request.append("\"");
    }

    public boolean unquoted(TypeInfo type) {
        return type.isPrimitive()
                || Boolean.class.isAssignableFrom(type.getRawType())
                || Number.class.isAssignableFrom(type.getRawType())
                || type.isEnum();
    }

    private void buildArrayParam(TypeInfo itemType, List<?> values) {
        request.append("[");
        Repeated repeated = new Repeated(", ");
        values.forEach(value -> {
            request.append(repeated);
            buildParam(itemType, value);
        });
        request.append("]");
    }

    private void buildObjectParam(TypeInfo type, Object value) {
        request.append("{");
        Repeated repeated = new Repeated(", ");
        type.fields().forEach(field -> {
            request.append(repeated);
            request.append(field.getName()).append(": ");
            buildParam(field.getType(), field.get(value));
        });
        request.append("}");
    }

    private static class Repeated {
        private final String text;
        private boolean first = true;

        public Repeated(String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            if (first) {
                first = false;
                return "";
            }
            return text;
        }
    }
}
