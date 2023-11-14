package io.smallrye.graphql.index.generic;

public interface Attribute<T> {
    T getValue();

    void setValue(T value);
}
