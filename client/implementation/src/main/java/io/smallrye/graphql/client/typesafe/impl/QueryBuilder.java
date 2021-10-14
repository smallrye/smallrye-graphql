package io.smallrye.graphql.client.typesafe.impl;

import static java.util.stream.Collectors.joining;

import java.util.Stack;

import io.smallrye.graphql.client.SmallRyeGraphQLClientMessages;
import io.smallrye.graphql.client.typesafe.impl.reflection.FieldInfo;
import io.smallrye.graphql.client.typesafe.impl.reflection.MethodInvocation;
import io.smallrye.graphql.client.typesafe.impl.reflection.ParameterInfo;
import io.smallrye.graphql.client.typesafe.impl.reflection.TypeInfo;

public class QueryBuilder {
    private final MethodInvocation method;
    private final Stack<String> typeStack = new Stack<>();
    private final Stack<String> expressionStack = new Stack<>();

    public QueryBuilder(MethodInvocation method) {
        this.method = method;
    }

    public String build() {
        StringBuilder request = new StringBuilder();
        request.append(method.isQuery() ? "query " : "mutation ");
        request.append(method.getName());
        if (method.hasValueParameters())
            request.append(method.valueParameters().map(this::declare).collect(joining(", ", "(", ")")));

        if (method.isSingle()) {
            request.append(" { ");
            request.append(method.getName());
            if (method.hasRootParameters())
                request.append(method.rootParameters()
                        .map(this::bind)
                        .collect(joining(", ", "(", ")")));
        }

        request.append(fields(method.getReturnType()));

        if (method.isSingle())
            request.append(" }");

        return request.toString();
    }

    private String declare(ParameterInfo parameter) {
        return "$" + parameter.getRawName() + ": " + parameter.graphQlInputTypeName();
    }

    private String bind(ParameterInfo parameter) {
        return parameter.getName() + ": $" + parameter.getRawName();
    }

    private String fields(TypeInfo type) {
        if (typeStack.contains(type.getTypeName()))
            throw SmallRyeGraphQLClientMessages.msg.fieldRecursionFound();
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
        StringBuilder expression = new StringBuilder();
        field.getAlias().ifPresent(alias -> expression.append(alias).append(":"));
        expression.append(field.getName());
        if (!type.isScalar() && (!type.isCollection() || !type.getItemType().isScalar())) {
            String path = nestedExpressionPrefix() + field.getRawName();
            if (method.hasNestedParameters(path))
                expression.append(method.nestedParameters(path)
                        .map(this::bind)
                        .collect(joining(", ", "(", ")")));
            expressionStack.push(path);
            expression.append(fields(type));
            expressionStack.pop();
        }
        return expression.toString();
    }

    private String nestedExpressionPrefix() {
        return expressionStack.isEmpty() ? "" : expressionStack.peek() + ".";
    }
}
