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

    public ParameterInfo(MethodInvocation method, Parameter parameter, Object value) {
        this.method = method;
        this.parameter = parameter;
        this.type = new TypeInfo(null, parameter.getType(), parameter.getAnnotatedType());
        this.value = value;
    }

    @Override
    public String toString() {
        return "parameter '" + parameter.getName() + "' in " + method;
    }

    public String graphQlInputTypeName() {
        if (type.isScalar()) {
            return graphQlInputTypeName(type) + optionalExclamationMark(type);
        } else if (type.isCollection()) {
            return "[" + withExclamationMark(type.getItemType()) + "]" + optionalExclamationMark(type);
        } else {
            return withExclamationMark(type);
        }
    }

    private String withExclamationMark(TypeInfo itemType) {
        return graphQlInputTypeName(itemType) + optionalExclamationMark(itemType);
    }

    private String graphQlInputTypeName(TypeInfo type) {
        return simpleInputTypeName(type);
    }

    private String simpleInputTypeName(TypeInfo type) {
        if (type.isAnnotated(Name.class))
            return type.getAnnotation(Name.class).value() + "Input";
        switch (type.getSimpleName()) {
            case "boolean":
            case "Boolean":
                return "Boolean";
            case "int":
            case "Integer":
            case "long":
            case "Long":
                return "Int";
            default:
                return type.getSimpleName() + (type.isScalar() || type.isEnum() ? "" : "Input");
        }
    }

    private String optionalExclamationMark(TypeInfo itemType) {
        return itemType.isNonNull() ? "!" : "";
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
        return parameter.getAnnotationsByType((Class<? extends Annotation>) Header.class).length > 0;
    }

    public boolean isValueParameter() {
        return !isHeaderParameter();
    }
}
