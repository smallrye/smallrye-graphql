package io.smallrye.graphql.client.impl.core.factory;

import io.smallrye.graphql.client.core.Operation;
import io.smallrye.graphql.client.core.factory.OperationFactory;
import io.smallrye.graphql.client.impl.core.OperationImpl;

public class OperationFactoryImpl implements OperationFactory {

    @Override
    public Operation get() {
        return new OperationImpl();
    }
}
