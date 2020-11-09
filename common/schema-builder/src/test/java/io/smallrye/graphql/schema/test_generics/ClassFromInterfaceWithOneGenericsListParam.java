package io.smallrye.graphql.schema.test_generics;

import java.util.List;

public class ClassFromInterfaceWithOneGenericsListParam implements InterfaceWithOneGenericsListParam<ClassWithoutGenerics> {

    @Override
    public List<ClassWithoutGenerics> getInstance() {
        return null;
    }

    @Override
    public String getName() {
        return "name";
    }

}
