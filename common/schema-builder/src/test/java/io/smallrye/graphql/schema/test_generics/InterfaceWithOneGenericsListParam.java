package io.smallrye.graphql.schema.test_generics;

import java.util.List;

public interface InterfaceWithOneGenericsListParam<T> {

    List<T> getInstance();

    String getName();

}
