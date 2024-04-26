package io.smallrye.graphql.client.model;

import java.util.Arrays;
import java.util.Objects;

public class MethodKey {
    private final Class<?> declaringClass;
    private final String methodName;
    private final Class<?>[] parameterTypes;

    public MethodKey(Class<?> declaringClass, String methodName, Class<?>[] parameterTypes) {
        this.declaringClass = declaringClass;
        this.methodName = methodName;
        this.parameterTypes = parameterTypes;
    }

    public MethodKey() {
        this.declaringClass = null;
        this.methodName = null;
        this.parameterTypes = null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        MethodKey methodKey = (MethodKey) o;
        return Objects.equals(declaringClass, methodKey.declaringClass) && Objects.equals(methodName, methodKey.methodName)
                && Arrays.equals(parameterTypes, methodKey.parameterTypes);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(declaringClass, methodName);
        result = 31 * result + Arrays.hashCode(parameterTypes);
        return result;
    }

    public Class<?> getDeclaringClass() {
        return declaringClass;
    }

    public String getMethodName() {
        return methodName;
    }

    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    @Override
    public String toString() {
        return "MethodKey{" +
                "declaringClass=" + declaringClass +
                ", methodName='" + methodName + '\'' +
                ", parameterTypes=" + Arrays.toString(parameterTypes) +
                '}';
    }
}
