package io.smallrye.graphql.test.apps.generics.api;

public class ClassWithOneGenericsParamToString2 extends ClassWithOneGenericsParamToString {

    public ClassWithOneGenericsParamToString2() {
        super();
    }

    public ClassWithOneGenericsParamToString2(String s) {
        super(s);
    }

    public String getMe() {
        return "me";
    }

}
