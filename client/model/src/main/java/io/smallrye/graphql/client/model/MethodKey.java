package io.smallrye.graphql.client.model;

import java.util.Arrays;
import java.util.Objects;

/**
 * Represents a unique identifier for an Client API operation (method). This class
 * combines the method name and its parameter types to create a key that can
 * be identified by both Jandex and Java reflection.
 *
 * @author mskacelik
 */
public class MethodKey {
    private String methodName;
    private Class<?>[] parameterTypes;

    public MethodKey(String methodName, Class<?>[] parameterTypes) {
        this.methodName = methodName;
        this.parameterTypes = parameterTypes;
    }

    public MethodKey() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        MethodKey methodKey = (MethodKey) o;
        return Objects.equals(methodName, methodKey.methodName) && Arrays.equals(parameterTypes, methodKey.parameterTypes);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(methodName);
        result = 31 * result + Arrays.hashCode(parameterTypes);
        return result;
    }

    // set methods for bytecode recording
    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(Class<?>[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    @Override
    public String toString() {
        return "MethodKey{" +
                "methodName='" + methodName + '\'' +
                ", parameterTypes=" + Arrays.toString(parameterTypes) +
                '}';
    }
}
