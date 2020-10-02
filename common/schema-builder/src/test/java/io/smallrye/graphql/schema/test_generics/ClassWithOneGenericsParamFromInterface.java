package io.smallrye.graphql.schema.test_generics;

public class ClassWithOneGenericsParamFromInterface<V> implements InterfaceWithOneGenericsParam<V> {

    public V getInstance() {
        return null;
    }

    public String getName() {
        return null;
    }

}
