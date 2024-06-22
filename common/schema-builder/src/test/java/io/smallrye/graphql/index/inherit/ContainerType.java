package io.smallrye.graphql.index.inherit;

public class ContainerType implements ContainerInterface {

    public FieldType getInheritField() {
        return new FieldType();
    }
}
