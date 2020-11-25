package io.smallrye.graphql.test.apps.generics.api;

public class ClassWithOneGenericsParam<T> {

    T param1;

    public ClassWithOneGenericsParam() {

    }

    public ClassWithOneGenericsParam(T param1) {
        this.param1 = param1;
    }

    public T getParam1() {
        return param1;
    }

    public void setParam1(T param1) {
        this.param1 = param1;
    }

    public String getName() {
        return "name";
    }

}
