package io.smallrye.graphql.test;

public class ObjectWithColor implements UnionInterfaceTwo {

    private String color;

    public ObjectWithColor() {
    }

    public ObjectWithColor(String color) {
        this.color = color;
    }

    @Override
    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
