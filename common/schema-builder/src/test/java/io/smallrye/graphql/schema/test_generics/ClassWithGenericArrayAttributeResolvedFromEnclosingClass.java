package io.smallrye.graphql.schema.test_generics;

import java.util.Date;

public class ClassWithGenericArrayAttributeResolvedFromEnclosingClass<V> {

    public ClassWithTwoGenericsParams<V, Date>[] getParam1() {
        return null;
    }

    public String getName() {
        return null;
    }
}
