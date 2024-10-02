package io.smallrye.graphql.client.impl.core.factory;

import io.smallrye.graphql.client.core.Field;
import io.smallrye.graphql.client.core.factory.FieldFactory;
import io.smallrye.graphql.client.impl.core.FieldImpl;

public class FieldFactoryImpl implements FieldFactory {

    @Override
    public Field get() {
        return new FieldImpl();
    }
}
