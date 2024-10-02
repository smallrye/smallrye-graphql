package io.smallrye.graphql.client.impl.core.factory;

import io.smallrye.graphql.client.core.InlineFragment;
import io.smallrye.graphql.client.core.factory.InlineFragmentFactory;
import io.smallrye.graphql.client.impl.core.InlineFragmentImpl;

public class InlineFragmentFactoryImpl implements InlineFragmentFactory {

    @Override
    public InlineFragment get() {
        return new InlineFragmentImpl();
    }
}
