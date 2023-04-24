package io.smallrye.graphql.test.apps.adapt.to.api;

/**
 * Testing AdaptTo using a static getInstance Method
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class WithGetInstance {

    private String foo;

    public String getFoo() {
        return foo;
    }

    public void setFoo(String foo) {
        this.foo = foo;
    }

    public static WithGetInstance getInstance(String foo) {
        WithGetInstance withGetInstance = new WithGetInstance();
        withGetInstance.foo = foo;
        return withGetInstance;
    }

    @Override
    public String toString() {
        return foo;
    }

}
