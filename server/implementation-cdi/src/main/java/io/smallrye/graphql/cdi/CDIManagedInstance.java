package io.smallrye.graphql.cdi;

import javax.enterprise.inject.Instance;

import io.smallrye.graphql.spi.ManagedInstance;

public class CDIManagedInstance<T> implements ManagedInstance<T> {

    private final Instance<T> instance;
    private final T object;
    private final boolean isDependentScoped;

    CDIManagedInstance(Instance<T> instance, boolean isDependentScoped) {
        this.instance = instance;
        this.isDependentScoped = isDependentScoped;
        this.object = instance.get();
    }

    @Override
    public T get() {
        return object;
    }

    @Override
    public void destroyIfNecessary() {
        if (isDependentScoped) {
            instance.destroy(object);
        }
    }
}
