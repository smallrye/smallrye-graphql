package io.smallrye.graphql.test.apps.interfaces.api;

import java.io.Serializable;

public class MyClass {

    private Serializable something;

    public String getSomething() {
        return something.toString();
    }

    public void setSomething(String something) {
        this.something = something;
    }
}