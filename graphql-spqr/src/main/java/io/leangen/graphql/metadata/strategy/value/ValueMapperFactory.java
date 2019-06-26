package io.leangen.graphql.metadata.strategy.value;

import java.util.List;
import java.util.Map;

import io.leangen.graphql.execution.GlobalEnvironment;

/**
 * @author Bojan Tomic (kaqqao)
 */
public interface ValueMapperFactory {

    ValueMapper getValueMapper(Map<Class, List<Class<?>>> concreteSubTypes, GlobalEnvironment environment);
}
