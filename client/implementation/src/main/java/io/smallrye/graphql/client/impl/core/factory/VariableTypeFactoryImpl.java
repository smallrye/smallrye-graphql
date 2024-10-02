package io.smallrye.graphql.client.impl.core.factory;

import io.smallrye.graphql.client.core.VariableType;
import io.smallrye.graphql.client.core.factory.VariableTypeFactory;
import io.smallrye.graphql.client.impl.core.VariableTypeImpl;

public class VariableTypeFactoryImpl implements VariableTypeFactory {

    @Override
    public VariableType get() {
        return new VariableTypeImpl();
    }
}
