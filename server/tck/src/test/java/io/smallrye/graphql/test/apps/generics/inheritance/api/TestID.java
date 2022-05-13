package io.smallrye.graphql.test.apps.generics.inheritance.api;

public class TestID extends AbstractID<TestID> implements ID<TestID> {

    public TestID() {
        super("test");
    }

    public TestID(String id) {
        super(id);
    }

    @Override
    public String getValue() {
        return super.getValue();
    }

    @Override
    public void setValue(String id) {
        super.setValue(id);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
