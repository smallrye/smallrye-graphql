package io.smallrye.graphql.client.impl.typesafe.reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.stream.Stream;

import org.eclipse.microprofile.graphql.Id;
import org.eclipse.microprofile.graphql.Input;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.NonNull;

import io.smallrye.graphql.client.typesafe.api.Header;
import io.smallrye.graphql.client.typesafe.api.NestedParameter;

public class ParameterInfo implements NamedElement {
    private final MethodInvocation method;
    private final Parameter parameter;
    private final TypeInfo type;
    private final Object value;

    public ParameterInfo(MethodInvocation method, Parameter parameter, Object value, Type genericParameterType) {
        this.method = method;
        this.parameter = parameter;
        this.type = new TypeInfo(null,
                parameter.getType(),
                parameter.getAnnotatedType(),
                genericParameterType);
        this.value = value;
    }

    @Override
    public String toString() {
        return "parameter '" + parameter.getName() + "' in " + method;
    }

    public String graphQlInputTypeName() {
        if (parameter.isAnnotationPresent(Id.class)) {
            if (type.isCollection()) {
                return "[ID" + optionalExclamationMark(type.getItemType()) + "]" + optionalExclamationMark(type);
            } else {
                return "ID" + optionalExclamationMark(type);
            }
        } else if (type.isCollection()) {
            return "[" + withExclamationMark(type.getItemType()) + "]" + optionalExclamationMark(type);
        } else if (type.isMap()) {
            return "[Entry_" + withExclamationMark(type.getKeyType())
                    + "_" + withExclamationMark(type.getValueType()) + "Input]"
                    + optionalExclamationMark(type);
        } else {
            return withExclamationMark(type);
        }
    }

    private String withExclamationMark(TypeInfo itemType) {
        return graphQlInputTypeName(itemType) + optionalExclamationMark(itemType);
    }

    private String graphQlInputTypeName(TypeInfo type) {
        if (type.isAnnotated(Input.class)) {
            String value = type.getAnnotation(Input.class).value();
            if (!value.isEmpty()) {
                return value;
            }
        }
        if (type.isAnnotated(Name.class))
            return type.getAnnotation(Name.class).value();
        // Only because of the switch works with *java.lang.Date* which is *DateTime*
        if (type.getTypeName().equals("java.sql.Date")) {
            return "Date";
        }
        switch (type.getSimpleName()) {
            case "int":
            case "Integer":
            case "short":
            case "Short":
            case "byte":
            case "Byte":
            case "OptionalInt":
                return "Int";
            case "float":
            case "Float":
            case "double":
            case "Double":
            case "OptionalDouble":
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
            case "OptionalLong":
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
            case "Instant":
            case "Calendar":
            case "GregorianCalendar":
            case "Date": // java.lang.Date
                return "DateTime";
            default:
                return type.getSimpleName() + (type.isScalar() || type.isEnum() ? "" : "Input");
        }
    }

    private String optionalExclamationMark(TypeInfo itemType) {
        if (itemType == this.type) {
            // Work around a weird GraalVM native mode behavior in which the object returned by
            // parameter.getAnnotatedType() does not include annotation metadata. So if we're
            // determining whether this Parameter itself is nullable, introspect the Parameter
            // instance itself rather than its AnnotatedType (which is what the
            // `itemType.isNonNull` method would do).
            Class jakartaNotNullClass = null;
            try {
                jakartaNotNullClass = Class.forName("jakarta.validation.constraints.NotNull", false,
                        Thread.currentThread().getContextClassLoader());
            } catch (ClassNotFoundException e) {
                /* IN CASE THE CLASS IS NOT IMPORTED */
            }
            return this.type.isPrimitive() ||
                    (jakartaNotNullClass != null && parameter.isAnnotationPresent(jakartaNotNullClass)) ||
                    parameter.isAnnotationPresent(NonNull.class) ? "!" : "";
        } else {
            return itemType.isNonNull() ? "!" : "";
        }
    }

    public Object getValue() {
        return this.value;
    }

    @Override
    public String getName() {
        if (parameter.isAnnotationPresent(Name.class))
            return parameter.getAnnotation(Name.class).value();
        if (!parameter.isNamePresent())
            throw new RuntimeException("Missing name information for " + this + ".\n" +
                    "You can either annotate all parameters with @Name, " +
                    "or compile your source code with the -parameters options, " +
                    "so the parameter names are compiled into the class file and available at runtime.");
        return getRawName();
    }

    @Override
    public String getRawName() {
        return parameter.getName();
    }

    public <A extends Annotation> A[] getAnnotations(Class<A> type) {
        return parameter.getAnnotationsByType(type);
    }

    public boolean isHeaderParameter() {
        return parameter.isAnnotationPresent(Header.class);
    }

    public boolean isValueParameter() {
        return isRootParameter() || isNestedParameter();
    }

    public boolean isRootParameter() {
        return !isHeaderParameter() && !isNestedParameter();
    }

    public boolean isNestedParameter() {
        return parameter.isAnnotationPresent(NestedParameter.class);
    }

    public Stream<String> getNestedParameterNames() {
        return Stream.of(parameter.getAnnotation(NestedParameter.class).value());
    }
}
