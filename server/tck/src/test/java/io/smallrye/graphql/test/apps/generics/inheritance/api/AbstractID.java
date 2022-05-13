package io.smallrye.graphql.test.apps.generics.inheritance.api;

public abstract class AbstractID<I extends ID<I>> {

    private String id;

    public AbstractID(final String id) {
        this.id = id;
    }

    public String getValue() {
        return this.id;
    }

    public void setValue(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return this.id;
    }
}
