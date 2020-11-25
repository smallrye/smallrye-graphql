package io.smallrye.graphql.test.apps.generics.api;

import java.util.Arrays;
import java.util.List;

public class ClassFromInterfaceWithOneGenericsListParam implements InterfaceWithOneGenericsListParam<ClassWithoutGenerics> {

    List<ClassWithoutGenerics> instance;

    public ClassFromInterfaceWithOneGenericsListParam() {
    }

    public ClassFromInterfaceWithOneGenericsListParam(ClassWithoutGenerics... classWithoutGenerics) {
        this.instance = Arrays.asList(classWithoutGenerics);
    }

    @Override
    public List<ClassWithoutGenerics> getInstance() {
        return instance;
    }

    public void setInstance(List<ClassWithoutGenerics> instance) {
        this.instance = instance;
    }

    @Override
    public String getName() {
        return "name";
    }

}
