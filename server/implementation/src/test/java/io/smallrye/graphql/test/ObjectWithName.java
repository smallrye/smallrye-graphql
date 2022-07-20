package io.smallrye.graphql.test;

public class ObjectWithName implements UnionInterfaceOne {

    private String name;

    public ObjectWithName() {
    }

    public ObjectWithName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
