package io.smallrye.graphql.client.impl.typesafe;

import static java.util.stream.Collectors.joining;

import java.util.List;
import java.util.Stack;

import io.smallrye.graphql.client.impl.SmallRyeGraphQLClientMessages;
import io.smallrye.graphql.client.impl.typesafe.reflection.FieldInfo;
import io.smallrye.graphql.client.impl.typesafe.reflection.MethodInvocation;
import io.smallrye.graphql.client.impl.typesafe.reflection.ParameterInfo;
import io.smallrye.graphql.client.impl.typesafe.reflection.TypeInfo;

public class QueryBuilder {
    private final MethodInvocation method;
    private final Stack<String> typeStack = new Stack<>();
    private final Stack<String> expressionStack = new Stack<>();

    public QueryBuilder(MethodInvocation method) {
        this.method = method;
    }

    public String build() {
        StringBuilder request = new StringBuilder();
        switch (method.getOperationType()) {
            case QUERY:
                request.append("query ");
                break;
            case MUTATION:
                request.append("mutation ");
                break;
            case SUBSCRIPTION:
                request.append("subscription ");
                break;
        }
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
        if (type.isCollection() || type.isAsync())
            return fields(type.getItemType());
        if (type.isMap()) {
            String keyFields = fields(type.getKeyType());
            String valueFields = fields(type.getValueType());
            return "{ key " + keyFields + " value " + valueFields + "}";
        }
        return type.fields()
                .map(this::field)
                .collect(joining(" ", " {", "}"));
    }

    private String field(FieldInfo field) {
        TypeInfo type = field.getType();
        StringBuilder expression = new StringBuilder();
        field.getAlias().ifPresent(alias -> expression.append(alias).append(":"));
        expression.append(field.getName());

        String path = nestedExpressionPrefix() + field.getRawName();
        List<ParameterInfo> nestedParameters = method.nestedParameters(path);
        if (!nestedParameters.isEmpty())
            expression.append(nestedParameters.stream()
                    .map(this::bind)
                    .collect(joining(", ", "(", ")")));

        expressionStack.push(path);
        expression.append(fields(type)); // appends the empty string, if the type is scalar, etc.
        expressionStack.pop();

        return expression.toString();
    }

    private String nestedExpressionPrefix() {
        return expressionStack.isEmpty() ? "" : expressionStack.peek() + ".";
    }
}
