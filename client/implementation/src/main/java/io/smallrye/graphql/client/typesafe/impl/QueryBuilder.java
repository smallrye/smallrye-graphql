package io.smallrye.graphql.client.typesafe.impl;

import static java.util.stream.Collectors.joining;

import java.util.Stack;

import io.smallrye.graphql.client.typesafe.api.GraphQlClientException;
import io.smallrye.graphql.client.typesafe.impl.reflection.FieldInfo;
import io.smallrye.graphql.client.typesafe.impl.reflection.MethodInvocation;
import io.smallrye.graphql.client.typesafe.impl.reflection.ParameterInfo;
import io.smallrye.graphql.client.typesafe.impl.reflection.TypeInfo;

class QueryBuilder {
    private final MethodInvocation method;
    private final Stack<String> typeStack = new Stack<>();

    public QueryBuilder(MethodInvocation method) {
        this.method = method;
    }

    String build() {
        StringBuilder request = new StringBuilder();
        request.append(method.isQuery() ? "query " : "mutation ");
        request.append(method.getName());
        if (method.hasValueParameters())
            request.append(method.valueParameters().map(this::declare).collect(joining(", ", "(", ")")));
        request.append(" { ");
        request.append(method.getName());
        if (method.hasValueParameters())
            request.append(method.valueParameters().map(this::bind).collect(joining(", ", "(", ")")));
        request.append(fields(method.getReturnType()));
        request.append(" }");
        return request.toString();
    }

    private String declare(ParameterInfo parameter) {
        return "$" + parameter.getName() + ": " + parameter.graphQlInputTypeName();
    }

    private String bind(ParameterInfo parameter) {
        return parameter.getName() + ": $" + parameter.getName();
    }

    private String fields(TypeInfo type) {
        if (typeStack.contains(type.getTypeName()))
            throw new GraphQlClientException("field recursion found");
        try {
            typeStack.push(type.getTypeName());

            return recursionCheckedFields(type);
        } finally {
            typeStack.pop();
        }
    }

    private String recursionCheckedFields(TypeInfo type) {
        while (type.isOptional() || type.isErrorOr())
            type = type.getItemType();

        if (type.isScalar())
            return "";
        if (type.isCollection())
            return fields(type.getItemType());
        return type.fields()
                .map(this::field)
                .collect(joining(" ", " {", "}"));
    }

    private String field(FieldInfo field) {
        TypeInfo type = field.getType();
        if (type.isScalar() || type.isCollection() && type.getItemType().isScalar()) {
            return field.getName();
        } else {
            return field.getName() + fields(type);
        }
    }
}
