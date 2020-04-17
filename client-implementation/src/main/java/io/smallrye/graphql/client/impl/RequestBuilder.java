package io.smallrye.graphql.client.impl;

import java.util.List;

import io.smallrye.graphql.client.impl.reflection.MethodInfo;
import io.smallrye.graphql.client.impl.reflection.ParameterInfo;
import io.smallrye.graphql.client.impl.reflection.TypeInfo;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class RequestBuilder {
    private final MethodInfo method;
    private final StringBuilder request = new StringBuilder();

    String build() {
        request.append(method.getName());
        if (method.getParameterCount() > 0) {
            request.append("(");
            Repeated repeated = new Repeated(", ");
            for (ParameterInfo parameterInfo : method.getParameters()) {
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
        if (value instanceof Boolean || value instanceof Number)
            request.append(value);
        else if (type.isScalar())
            buildScalarParam(value);
        else if (type.isCollection())
            buildArrayParam(type.getItemType(), (List<?>) value);
        else
            buildObjectParam(type, value);
    }

    private void buildScalarParam(Object value) {
        request
                .append("\"")
                .append(value.toString()
                        .replace("\"", "\\\"")
                        .replace("\n", "\\n"))
                .append("\"");
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

    @RequiredArgsConstructor
    private static class Repeated {
        private final String text;
        private boolean first = true;

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
