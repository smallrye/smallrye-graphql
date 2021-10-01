package io.smallrye.graphql.client.typesafe.impl.reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

import javax.json.bind.annotation.JsonbProperty;

import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.NonNull;

import io.smallrye.graphql.client.typesafe.api.GraphQLClientException;

public class FieldInfo {
    private final TypeInfo container;
    private final Field field;
    private final boolean includeIfNull;
    private final String name;

    private static final String JSONB_PROPERTY = "javax.json.bind.annotation.JsonbProperty";
    private static final String JACKSON_PROPERTY = "com.fasterxml.jackson.annotation.JsonProperty";

    FieldInfo(TypeInfo container, Field field) {
        this.container = container;
        this.field = field;
        JsonbProperty jsonbPropertyAnnotation = field.getAnnotation(JsonbProperty.class);
        if (jsonbPropertyAnnotation != null) {
            includeIfNull = jsonbPropertyAnnotation.nillable();
        } else {
            includeIfNull = false;
        }
        this.name = computeName();
    }

    @Override
    public String toString() {
        return "field '" + getRawName() + "' in " + container;
    }

    public TypeInfo getType() {
        return new TypeInfo(container, field.getGenericType());
    }

    public String getName() {
        return name;
    }

    private String computeName() {
        String name = null;
        if (field.isAnnotationPresent(Name.class)) {
            name = field.getAnnotation(Name.class).value();
        } else {
            Optional<Annotation> jsonbProperty = getAnnotationByClassName(JSONB_PROPERTY);
            if (jsonbProperty.isPresent()) {
                Object value = getAnnotationParameter(jsonbProperty.get(), "value");
                if (value != null && !((String) value).isEmpty()) {
                    name = (String) value;
                }
            }
            Optional<Annotation> jacksonProperty = getAnnotationByClassName(JACKSON_PROPERTY);
            if (jacksonProperty.isPresent()) {
                Object value = getAnnotationParameter(jacksonProperty.get(), "value");
                if (value != null && !((String) value).isEmpty()) {
                    name = (String) value;
                }
            }
        }
        if (name == null) {
            name = getRawName();
        }
        return name;
    }

    private Object getAnnotationParameter(Annotation annotation, String parameterName) {
        try {
            Method m = annotation.getClass().getMethod(parameterName);
            return m.invoke(annotation);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Optional<Annotation> getAnnotationByClassName(String className) {
        return Arrays.stream(field.getAnnotations())
                .filter(a -> a.annotationType().getName().equals(className))
                .findAny();
    }

    public String getRawName() {
        return field.getName();
    }

    /** If the field is renamed with a {@link Name} annotation, the real field name is used as an alias. */
    public Optional<String> getAlias() {
        if (field.isAnnotationPresent(Name.class))
            return Optional.of(getRawName());
        return Optional.empty();
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return field.getAnnotation(annotationClass);
    }

    public Object get(Object instance) {
        try {
            field.setAccessible(true);
            return field.get(instance);
        } catch (ReflectiveOperationException e) {
            throw new GraphQLClientException("can't get field " + this, e); // TODO test with static?
        }
    }

    public void set(Object instance, Object value) {
        try {
            field.setAccessible(true);
            field.set(instance, value);
        } catch (ReflectiveOperationException e) {
            // this code is unreachable: setAccessible also allows to change `final` fields
            throw new GraphQLClientException("can't set field " + this + " to " + value, e); // TODO test with static
        }
    }

    public boolean isNonNull() {
        return field.isAnnotationPresent(NonNull.class) || getType().isPrimitive();
    }

    public boolean isIncludeNull() {
        return includeIfNull;
    }
}
