package io.smallrye.graphql.test.apps.generics.api;

import java.util.List;

public interface InterfaceWithOneGenericsListParam<T> {

    List<T> getInstance();

    String getName();

}
