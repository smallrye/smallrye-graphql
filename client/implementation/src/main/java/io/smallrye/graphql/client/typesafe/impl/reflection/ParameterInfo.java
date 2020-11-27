package io.smallrye.graphql.client.typesafe.impl.reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;

import org.eclipse.microprofile.graphql.Id;
import org.eclipse.microprofile.graphql.Input;
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
        if (parameter.isAnnotationPresent(Id.class)) {
            return "ID" + optionalExclamationMark(type);
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
        if (type.isAnnotated(Input.class))
            return type.getAnnotation(Input.class).value();
        if (type.isAnnotated(Name.class))
            return type.getAnnotation(Name.class).value();
        switch (type.getSimpleName()) {
            case "int":
            case "Integer":
            case "short":
            case "Short":
            case "byte":
            case "Byte":
                return "Int";
            case "float":
            case "Float":
            case "double":
            case "Double":
                return "Float";
            case "String":
            case "char":
            case "Character":
            case "UUID":
                return "String";
            case "boolean":
            case "Boolean":
                return "Boolean";
            case "BigInteger":
            case "long":
            case "Long":
                return "BigInteger";
            case "BigDecimal":
                return "BigDecimal";
            case "LocalDate":
                return "Date";
            case "LocalTime":
            case "OffsetTime":
                return "Time";
            case "LocalDateTime":
            case "OffsetDateTime":
            case "ZonedDateTime":
                return "DateTime";
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
