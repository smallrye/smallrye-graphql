package io.smallrye.graphql.test;

/**
 * Test generic class. Implements generic interface to reproduce bug #472
 */
public class ClassWithOneGenericsParam<T> implements InterfaceWithOneGenericsParam<T> {

    T param1;
    String name;

    public ClassWithOneGenericsParam(T param1, String name) {
        super();
        this.param1 = param1;
        this.name = name;
    }

    @Override
    public T getParam1() {
        return param1;
    }

    @Override
    public String getName() {
        return name;
    }
}
