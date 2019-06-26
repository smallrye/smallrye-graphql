package io.leangen.graphql.generator.mapping.strategy;

import java.lang.reflect.AnnotatedType;
import java.util.List;

import io.leangen.graphql.generator.BuildContext;

public interface ImplementationDiscoveryStrategy {

    List<AnnotatedType> findImplementations(AnnotatedType type, String[] scanPackages, BuildContext buildContext);
}
