package io.smallrye.graphql.client.typesafe.impl.reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;

import org.eclipse.microprofile.graphql.Name;

import io.smallrye.graphql.client.typesafe.api.GraphQlClientException;
import io.smallrye.graphql.client.typesafe.api.Header;

public class ParameterInfo {
    private final MethodInvocation method;
    private final Parameter parameter;
    private final TypeInfo type;
    private final Object value;

    public ParameterInfo(MethodInvocation method, Parameter parameter, TypeInfo type, Object value) {
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
            throw new GraphQlClientException("Missing name information for " + this + ".\n" +
                    "You can either annotate all parameters with @Name, " +
                    "or compile your source code with the -parameters options, " +
                    "so the parameter names are compiled into the class file and available at runtime.");
        return parameter.getName();
    }

    public <A extends Annotation> A[] getAnnotations(Class<A> type) {
        return parameter.getAnnotationsByType(type);
    }

    public boolean isHeaderParameter() {
        return isAnnotated(Header.class);
    }

    public boolean isValueParameter() {
        return !isAnnotated(Header.class);
    }

    public boolean isAnnotated(Class<? extends Annotation> annotationClass) {
        return parameter.getAnnotationsByType(annotationClass).length > 0;
    }
}
