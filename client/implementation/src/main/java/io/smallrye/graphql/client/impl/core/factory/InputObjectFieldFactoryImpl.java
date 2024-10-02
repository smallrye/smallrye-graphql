package io.smallrye.graphql.client.impl.core.factory;

import io.smallrye.graphql.client.core.InputObjectField;
import io.smallrye.graphql.client.core.factory.InputObjectFieldFactory;
import io.smallrye.graphql.client.impl.core.InputObjectFieldImpl;

public class InputObjectFieldFactoryImpl implements InputObjectFieldFactory {

    @Override
    public InputObjectField get() {
        return new InputObjectFieldImpl();
    }
}
