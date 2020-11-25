package io.smallrye.graphql.test.apps.generics.api;

public class ClassWithOneGenericsParamToString extends ClassWithOneGenericsParam<String> {

    public ClassWithOneGenericsParamToString() {

    }

    public ClassWithOneGenericsParamToString(String s) {
        super(s);
    }

    public int getAge() {
        return 99;
    }
}
