package io.smallrye.graphql.client.impl.core.factory;

import io.smallrye.graphql.client.core.Enum;
import io.smallrye.graphql.client.core.factory.EnumFactory;
import io.smallrye.graphql.client.impl.core.EnumImpl;

public class EnumFactoryImpl implements EnumFactory {

    @Override
    public Enum get() {
        return new EnumImpl();
    }
}
