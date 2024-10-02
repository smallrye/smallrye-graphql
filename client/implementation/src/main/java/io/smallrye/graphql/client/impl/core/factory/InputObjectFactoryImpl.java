package io.smallrye.graphql.client.impl.core.factory;

import io.smallrye.graphql.client.core.InputObject;
import io.smallrye.graphql.client.core.factory.InputObjectFactory;
import io.smallrye.graphql.client.impl.core.InputObjectImpl;

public class InputObjectFactoryImpl implements InputObjectFactory {

    @Override
    public InputObject get() {
        return new InputObjectImpl();
    }
}
