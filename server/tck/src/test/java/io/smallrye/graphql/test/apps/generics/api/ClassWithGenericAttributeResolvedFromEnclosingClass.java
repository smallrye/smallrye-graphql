package io.smallrye.graphql.test.apps.generics.api;

public class ClassWithGenericAttributeResolvedFromEnclosingClass<V> {

    private V v;

    public ClassWithGenericAttributeResolvedFromEnclosingClass() {

    }

    public ClassWithGenericAttributeResolvedFromEnclosingClass(V v) {
        this.v = v;
    }

    public ClassWithTwoGenericsParams<V, Integer> getParam1() {
        return new ClassWithTwoGenericsParams(v, 6);
    }

    public String getName() {
        return "name";
    }
}
