package io.smallrye.graphql.test.apps.generics.api;

import org.eclipse.microprofile.graphql.Name;

@Name("ClassWithOneGenericsParamWithNameAnnotationChanged")
public class ClassWithOneGenericsParamWithNameAnnotation<T> {

    T param1;

    public ClassWithOneGenericsParamWithNameAnnotation() {

    }

    public ClassWithOneGenericsParamWithNameAnnotation(T param1) {
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
