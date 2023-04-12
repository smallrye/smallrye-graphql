package io.smallrye.graphql.test.apps.generics.inheritance.api;

// FIXME: This is a workaround for https://github.com/smallrye/smallrye-graphql/issues/1405 until resolved
// extending a type with a type parameter that references the child class causes infinite recursion
//public class TestID extends AbstractID<TestID> implements ID<TestID> {
public class TestID extends AbstractID implements ID<TestID> {

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
