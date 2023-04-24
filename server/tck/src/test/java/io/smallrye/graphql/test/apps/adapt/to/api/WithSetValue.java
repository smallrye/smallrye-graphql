package io.smallrye.graphql.test.apps.adapt.to.api;

/**
 * Testing AdaptTo using a setValue
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class WithSetValue {

    private String foo;

    public WithSetValue() {
    }

    public String getFoo() {
        return foo;
    }

    public void setFoo(String foo) {
        this.foo = foo;
    }

    public void setValue(String foo) {
        setFoo(foo);
    }

    @Override
    public String toString() {
        return foo;
    }

}
