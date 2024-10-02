package io.smallrye.graphql.client.impl.core.factory;

import io.smallrye.graphql.client.core.Variable;
import io.smallrye.graphql.client.core.factory.VariableFactory;
import io.smallrye.graphql.client.impl.core.VariableImpl;

public class VariableFactoryImpl implements VariableFactory {

    @Override
    public Variable get() {
        return new VariableImpl();
    }
}
