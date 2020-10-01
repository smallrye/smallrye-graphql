package io.smallrye.graphql.schema.test_generics;

import java.util.List;

public class ClassWithGenericListAttributeResolvedFromEnclosingClass<V> {

    public List<ClassWithTwoGenericsParams<V, Integer>> getParam1() {
        return null;
    }

    public String getName() {
        return null;
    }
}
