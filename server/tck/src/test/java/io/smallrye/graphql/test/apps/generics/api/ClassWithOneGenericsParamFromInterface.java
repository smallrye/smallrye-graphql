package io.smallrye.graphql.test.apps.generics.api;

public class ClassWithOneGenericsParamFromInterface<V> implements InterfaceWithOneGenericsParam<V> {

    V instance;

    public ClassWithOneGenericsParamFromInterface() {

    }

    public ClassWithOneGenericsParamFromInterface(V v) {
        this.instance = v;
    }

    public V getInstance() {
        return instance;
    }

    public void setInstance(V instance) {
        this.instance = instance;
    }

    public String getName() {
        return "name";
    }
}
