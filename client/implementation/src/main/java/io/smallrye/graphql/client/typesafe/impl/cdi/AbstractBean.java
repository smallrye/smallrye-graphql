package io.smallrye.graphql.client.typesafe.impl.cdi;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.util.AnnotationLiteral;

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
        return Collections.emptySet();
    }

    @Override
    public String getName() {
        return type.getName();
    }

    @Override
    public Set<Annotation> getQualifiers() {
        return new HashSet<>(asList(new AnnotationLiteral<Default>() {
        }, new AnnotationLiteral<Any>() {
        }));
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return Dependent.class;
    }

    @Override
    public Set<Class<? extends Annotation>> getStereotypes() {
        return Collections.emptySet();
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
    public boolean isNullable() {
        return false;
    }

    @Override
    public void destroy(T instance, CreationalContext<T> ctx) {
        ctx.release();
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + ": " + getName() + " with " + getQualifiers();
    }
}
