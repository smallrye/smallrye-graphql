package io.smallrye.graphql.client.typesafe.impl.reflection;

import java.util.Objects;
import java.util.Optional;

public class MethodResolver {
    private final TypeInfo callerType;
    private String expression;

    public MethodResolver(TypeInfo callerType, String expression) {
        this.callerType = callerType;
        this.expression = expression;
    }

    public MethodInvocation resolve() {
        TypeInfo ownerType;
        int lastDot = expression.lastIndexOf('.');
        if (lastDot >= 0) { // class.method specified
            ownerType = toClass(expression.substring(0, lastDot));
            expression = expression.substring(lastDot + 1);
        } else {
            ownerType = callerType;
        }

        MethodInvocation method = resolveEnclosing(ownerType, expression)
                .orElseThrow(() -> new RuntimeException("no no-arg method '" + expression + "' found in " + ownerType));

        if (!method.isAccessibleFrom(callerType))
            throw new RuntimeException(callerType.getTypeName() + " can't access " + method);

        return method;
    }

    private Optional<MethodInvocation> resolveEnclosing(TypeInfo type, String expression) {
        return type.enclosingTypes()
                .map(t -> t.getMethod(expression).orElse(null))
                .filter(Objects::nonNull)
                .findFirst();
    }

    private TypeInfo toClass(String className) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try {
            return TypeInfo.of(Class.forName(className, true, loader));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("class not found for expression '" + expression + "'", e);
        }
    }
}
