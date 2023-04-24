package io.smallrye.graphql.test.apps.adapt.to.api;

/**
 * Testing AdaptTo using a constructor
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class WithConstructor {

    private String foo;

    public WithConstructor() {
    }

    public WithConstructor(String foo) {
        this.foo = foo;
    }

    public String getFoo() {
        return foo;
    }

    public void setFoo(String foo) {
        this.foo = foo;
    }

    @Override
    public String toString() {
        return foo;
    }

}
