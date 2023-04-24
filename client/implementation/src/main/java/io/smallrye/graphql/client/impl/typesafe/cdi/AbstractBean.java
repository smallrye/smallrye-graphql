package io.smallrye.graphql.client.impl.typesafe.cdi;

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.util.AnnotationLiteral;

public abstract class AbstractBean<T> implements Bean<T> {
    protected final Class<T> type;

    public AbstractBean(Class<T> type) {
        this.type = type;
    }

    @Override
    public Class<?> getBeanClass() {
        return type;
    }

    @Override
    public Set<InjectionPoint> getInjectionPoints() {
        return emptySet();
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Set<Annotation> getQualifiers() {
        return new HashSet<>(asList(
                new AnnotationLiteral<Default>() {
                },
                new AnnotationLiteral<Any>() {
                }));
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return Dependent.class;
    }

    @Override
    public Set<Class<? extends Annotation>> getStereotypes() {
        return emptySet();
    }

    @Override
    public Set<Type> getTypes() {
        return singleton(type);
    }

    @Override
    public boolean isAlternative() {
        return false;
    }

    @Override
    public void destroy(T instance, CreationalContext<T> ctx) {
        ctx.release();
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + ": " + type.getName() + " with " + getQualifiers();
    }
}
