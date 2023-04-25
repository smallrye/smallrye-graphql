package io.smallrye.graphql.index.generic;

import java.util.List;

public interface CharacterResource<T extends Character> {
    List<T> getAll();

    T add(T character);

    T remove(T character);

    T update(T character);

    T doSomething();
}