package io.smallrye.graphql.test.apps.generics.api;

public class ClassWithTwoGenericsParams<T, V> {

    T param1;
    V param2;

    public ClassWithTwoGenericsParams() {

    }

    public ClassWithTwoGenericsParams(T param1, V param2) {
        this.param1 = param1;
        this.param2 = param2;
    }

    public T getParam1() {
        return param1;
    }

    public V getParam2() {
        return param2;
    }

    public void setParam1(T param1) {
        this.param1 = param1;
    }

    public void setParam2(V param2) {
        this.param2 = param2;
    }

    public String getName() {
        return "name";
    }

}
