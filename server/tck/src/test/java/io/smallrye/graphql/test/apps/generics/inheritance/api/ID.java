package io.smallrye.graphql.test.apps.generics.inheritance.api;

public interface ID<I extends ID<I>> {

    // Marker interface.
}
