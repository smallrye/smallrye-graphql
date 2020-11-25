package io.smallrye.graphql.test.apps.generics.api;

public interface InterfaceWithOneGenericsParam<T> {

    T getInstance();

    String getName();

}
