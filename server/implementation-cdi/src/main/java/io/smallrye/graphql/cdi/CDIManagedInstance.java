package io.smallrye.graphql.cdi;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Instance;

import io.smallrye.graphql.spi.ManagedInstance;

public class CDIManagedInstance<T> implements ManagedInstance<T> {

    private final Instance.Handle<T> instanceHandle;
    private final T object;

    CDIManagedInstance(Instance.Handle<T> handle) {
        this.instanceHandle = handle;
        this.object = handle.get();
    }

    @Override
    public T get() {
        return object;
    }

    @Override
    public void destroyIfNecessary() {
        if (instanceHandle.getBean().getScope().equals(Dependent.class)) {
            instanceHandle.destroy();
        }
    }
}
