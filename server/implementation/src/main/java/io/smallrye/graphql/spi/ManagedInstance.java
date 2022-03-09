package io.smallrye.graphql.spi;

public interface ManagedInstance<T> {

    T get();

    void destroyIfNecessary();

}
