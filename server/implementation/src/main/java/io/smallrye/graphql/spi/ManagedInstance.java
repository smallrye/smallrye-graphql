package io.smallrye.graphql.spi;

public interface ManagedInstance<T> {

    T get();

    default void destroyIfNecessary() {
        // nothing
    }

}
