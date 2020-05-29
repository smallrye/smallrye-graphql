package io.smallrye.graphql.client.typesafe.impl.reflection;

import io.smallrye.graphql.client.typesafe.impl.CollectionUtils;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Query;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MethodInfo {
    public static MethodInfo of(Method method, Object... args) {
        return new MethodInfo(new TypeInfo(null, method.getDeclaringClass()), method, args);
    }

    private final TypeInfo type;
    private final Method method;
    private final Object[] parameterValues;

    private MethodInfo(TypeInfo type, Method method, Object[] parameterValues) {
        this.type = type;
        this.method = method;
        this.parameterValues = parameterValues;
    }

    @Override
    public String toString() {
        return type + "#" + method.getName();
    }

    public boolean isQuery() {
        return !ifAnnotated(Mutation.class).isPresent();
    }

    public String getName() {
        return queryName()
                .orElseGet(() -> mutationName()
                        .orElseGet(this::methodName));
    }

    private Optional<String> queryName() {
        return ifAnnotated(Query.class)
                .map(Query::value)
                .filter(CollectionUtils::nonEmpty);
    }

    private Optional<String> mutationName() {
        return ifAnnotated(Mutation.class)
                .map(Mutation::value)
                .filter(CollectionUtils::nonEmpty);
    }

    private String methodName() {
        String name = method.getName();
        if (name.startsWith("get") && name.length()>3 && Character.isUpperCase(name.charAt(3)))
            return Character.toLowerCase(name.charAt(3)) + name.substring(4);
        return name;
    }

    private <T extends Annotation> Optional<T> ifAnnotated(Class<T> type) {
        return Optional.ofNullable(method.getAnnotation(type));
    }

    public int getParameterCount() {
        return method.getParameterCount();
    }

    public TypeInfo getReturnType() {
        return new TypeInfo(type, method.getGenericReturnType(), returnTypeAnnotations());
    }

    private AnnotatedType[] returnTypeAnnotations() {
        if (method.getAnnotatedReturnType() instanceof AnnotatedParameterizedType)
            return ((AnnotatedParameterizedType) method.getAnnotatedReturnType()).getAnnotatedActualTypeArguments();
        else
            return new AnnotatedType[0];
    }

    public List<ParameterInfo> getParameters() {
        Parameter[] parameters = method.getParameters();
        assert parameters.length == parameterValues.length;
        List<ParameterInfo> list = new ArrayList<>();
        for (int i = 0; i < parameters.length; i++) {
            list.add(new ParameterInfo(this,
                    parameters[i],
                    new TypeInfo(null, method.getGenericParameterTypes()[i]),
                    parameterValues[i]));
        }
        return list;
    }
}
