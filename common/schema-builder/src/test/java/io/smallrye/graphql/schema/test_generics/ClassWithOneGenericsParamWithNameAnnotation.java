package io.smallrye.graphql.schema.test_generics;

import org.eclipse.microprofile.graphql.Name;

@Name("ClassWithOneGenericsParamWithNameAnnotationChanged")
public class ClassWithOneGenericsParamWithNameAnnotation<T> {

    public T getParam1() {
        return null;
    }

    public String getName() {
        return null;
    }
}
