package io.smallrye.graphql.client.typesafe.impl.reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;

import org.eclipse.microprofile.graphql.Name;

import io.smallrye.graphql.client.typesafe.api.GraphQlClientException;

public class ParameterInfo {
    private final MethodInfo method;
    private final Parameter parameter;
    private final TypeInfo type;
    private final Object value;

    public ParameterInfo(MethodInfo method, Parameter parameter, TypeInfo type, Object value) {
        this.method = method;
        this.parameter = parameter;
        this.type = type;
        this.value = value;
    }

    @Override
    public String toString() {
        return "parameter '" + parameter.getName() + "' in " + method;
    }

    public TypeInfo getType() {
        return this.type;
    }

    public Object getValue() {
        return this.value;
    }

    public String getName() {
        if (parameter.isAnnotationPresent(Name.class))
            return parameter.getAnnotation(Name.class).value();
        if (!parameter.isNamePresent())
            throw new GraphQlClientException("compile with -parameters to add the parameter names to the class file");
        return parameter.getName();
    }

    public <A extends Annotation> A[] getAnnotations(Class<A> type) {
        return parameter.getAnnotationsByType(type);
    }

    public boolean isAnnotated(Class<? extends Annotation> annotationClass) {
        return parameter.getAnnotationsByType(annotationClass).length > 0;
    }
}
