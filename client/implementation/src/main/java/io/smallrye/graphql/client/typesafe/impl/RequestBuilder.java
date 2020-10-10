package io.smallrye.graphql.client.typesafe.impl;

import static java.util.stream.Collectors.toList;

import java.util.List;

import io.smallrye.graphql.client.typesafe.api.Header;
import io.smallrye.graphql.client.typesafe.impl.reflection.MethodInfo;
import io.smallrye.graphql.client.typesafe.impl.reflection.ParameterInfo;

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
        request.append(parameter.getName())
                .append(": $")
                .append(parameter.getName());
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
