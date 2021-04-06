package io.smallrye.graphql.client.typesafe.impl.reflection;

import java.lang.reflect.Field;
import java.util.Optional;

import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.NonNull;

import io.smallrye.graphql.client.typesafe.api.GraphQlClientException;

public class FieldInfo {
    private final TypeInfo container;
    private final Field field;

    FieldInfo(TypeInfo container, Field field) {
        this.container = container;
        this.field = field;
    }

    @Override
    public String toString() {
        return "field '" + field.getName() + "' in " + container;
    }

    public TypeInfo getType() {
        return new TypeInfo(container, field.getGenericType());
    }

    public String getName() {
        if (field.isAnnotationPresent(Name.class))
            return field.getAnnotation(Name.class).value();
        return field.getName();
    }

    /** If the field is renamed with a {@link Name} annotation, the real field name is used as an alias. */
    public Optional<String> getAlias() {
        if (field.isAnnotationPresent(Name.class))
            return Optional.of(field.getName());
        return Optional.empty();
    }

    public Object get(Object instance) {
        try {
            field.setAccessible(true);
            return field.get(instance);
        } catch (ReflectiveOperationException e) {
            throw new GraphQlClientException("can't get field " + this, e); // TODO test with static?
        }
    }

    public void set(Object instance, Object value) {
        try {
            field.setAccessible(true);
            field.set(instance, value);
        } catch (ReflectiveOperationException e) {
            // this code is unreachable: setAccessible also allows to change `final` fields
            throw new GraphQlClientException("can't set field " + this + " to " + value, e); // TODO test with static
        }
    }

    public boolean isNonNull() {
        return field.isAnnotationPresent(NonNull.class) || getType().isPrimitive();
    }
}
