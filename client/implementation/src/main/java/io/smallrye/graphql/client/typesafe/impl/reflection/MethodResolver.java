package io.smallrye.graphql.client.typesafe.impl.reflection;

import io.smallrye.graphql.client.typesafe.api.GraphQlClientException;

public class MethodResolver {
    private final TypeInfo callerType;
    private String expression;

    public MethodResolver(TypeInfo callerType, String expression) {
        this.callerType = callerType;
        this.expression = expression;
    }

    public MethodInfo resolve() {
        TypeInfo ownerType;
        int lastDot = expression.lastIndexOf('.');
        if (lastDot >= 0) { // class.method specified
            ownerType = toClass(expression.substring(0, lastDot));
            expression = expression.substring(lastDot + 1);
        } else {
            ownerType = callerType;
        }

        MethodInfo method = ownerType.getMethod(expression)
                .orElseThrow(() -> new GraphQlClientException("no no-arg method '" + expression + "' found in " + ownerType));

        if (!method.isAccessibleFrom(callerType))
            throw new GraphQlClientException(callerType.getTypeName() + " can't access " + method);

        return method;
    }

    private TypeInfo toClass(String className) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try {
            return TypeInfo.of(Class.forName(className, true, loader));
        } catch (ClassNotFoundException e) {
            throw new GraphQlClientException("class not found for expression '" + expression + "'", e);
        }
    }
}
