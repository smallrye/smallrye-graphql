package io.smallrye.graphql.client.impl.core.factory;

import io.smallrye.graphql.client.core.Fragment;
import io.smallrye.graphql.client.core.factory.FragmentFactory;
import io.smallrye.graphql.client.impl.core.FragmentImpl;

public class FragmentFactoryImpl implements FragmentFactory {

    @Override
    public Fragment get() {
        return new FragmentImpl();
    }
}
