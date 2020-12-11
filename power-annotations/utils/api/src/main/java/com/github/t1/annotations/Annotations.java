package com.github.t1.annotations;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.stream.Stream;

public interface Annotations {
    static Annotations on(Class<?> type) {
        return AnnotationsLoader.INSTANCE.onType(type);
    }

    static Annotations on(Field field) {
        return onField(field.getDeclaringClass(), field.getName());
    }

    static Annotations onField(Class<?> type, String fieldName) {
        return AnnotationsLoader.INSTANCE.onField(type, fieldName);
    }

    static Annotations on(Method method) {
        return onMethod(method.getDeclaringClass(), method.getName(), method.getParameterTypes());
    }

    static Annotations onMethod(Class<?> type, String methodName, Class<?>... argTypes) {
        return AnnotationsLoader.INSTANCE.onMethod(type, methodName, argTypes);
    }

    /**
     * Get all {@link Annotation} instances.
     * If the annotation type is {@link java.lang.annotation.Repeatable}, the same type
     * can show up several times, eventually with different properties.
     */
    Stream<Annotation> all();

    /**
     * Get the 'strongest' {@link Annotation} instance of this type.
     * Multiple annotations may be applicable, e.g. from several mixins or stereotypes.
     * The annotation will be picked in this order:
     * <ol>
     * <li>mixin</li>
     * <li>target</li>
     * <li>target stereotypes</li>
     * <li>containing class</li>
     * <li>containing class stereotypes</li>
     * <li>containing package (TODO not yet implemented)</li>
     * <li>containing package stereotypes (TODO not yet implemented)</li>
     * </ol>
     * If this order not sufficiently defined, e.g. because there are multiple repeatable annotations,
     * or because there are multiple mixins or stereotypes with the same annotation,
     * an {@link AmbiguousAnnotationResolutionException} is thrown. I.e. when an annotation is changed
     * to be repeatable, all frameworks using this annotation will have to use {@link #all(Class)} instead
     * (of course), but the semantics for the client code changes, which may break existing code.
     */
    <T extends Annotation> Optional<T> get(Class<T> type);

    /**
     * Get all (eventually {@link java.lang.annotation.Repeatable repeatable})
     * {@link Annotation} instances of this <code>type</code>.
     */
    <T extends Annotation> Stream<T> all(Class<T> type);
}
