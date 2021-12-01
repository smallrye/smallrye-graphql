package io.smallrye.graphql.test.apps.adapt.to.api;

/**
 * Testing AdaptTo using a static fromXXX Method
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class WithStaticFrom {

    private String foo;

    public String getFoo() {
        return foo;
    }

    public void setFoo(String foo) {
        this.foo = foo;
    }

    public static WithStaticFrom fromString(String foo) {
        WithStaticFrom withStaticFrom = new WithStaticFrom();
        withStaticFrom.foo = foo;
        return withStaticFrom;
    }

    @Override
    public String toString() {
        return foo;
    }
}
