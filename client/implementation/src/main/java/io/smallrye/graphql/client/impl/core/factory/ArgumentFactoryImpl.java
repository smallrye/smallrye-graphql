package io.smallrye.graphql.client.impl.core.factory;

import io.smallrye.graphql.client.core.Argument;
import io.smallrye.graphql.client.core.factory.ArgumentFactory;
import io.smallrye.graphql.client.impl.core.ArgumentImpl;

public class ArgumentFactoryImpl implements ArgumentFactory {

    @Override
    public Argument get() {
        return new ArgumentImpl();
    }
}
