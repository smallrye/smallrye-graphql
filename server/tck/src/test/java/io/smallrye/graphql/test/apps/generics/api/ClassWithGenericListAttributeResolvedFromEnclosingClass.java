package io.smallrye.graphql.test.apps.generics.api;

import java.util.ArrayList;
import java.util.List;

public class ClassWithGenericListAttributeResolvedFromEnclosingClass<V> {

    private V v;

    public ClassWithGenericListAttributeResolvedFromEnclosingClass() {

    }

    public ClassWithGenericListAttributeResolvedFromEnclosingClass(V v) {
        this.v = v;
    }

    public List<ClassWithTwoGenericsParams<V, Integer>> getParam1() {
        List<ClassWithTwoGenericsParams<V, Integer>> classWithTwoGenericsParamses = new ArrayList<>();
        classWithTwoGenericsParamses.add(new ClassWithTwoGenericsParams<>(v, 7));
        return classWithTwoGenericsParamses;
    }

    public String getName() {
        return "name";
    }
}
