package io.smallrye.graphql.client.impl.core.factory;

import io.smallrye.graphql.client.core.FragmentReference;
import io.smallrye.graphql.client.core.factory.FragmentReferenceFactory;
import io.smallrye.graphql.client.impl.core.FragmentReferenceImpl;

public class FragmentReferenceFactoryImpl implements FragmentReferenceFactory {

    @Override
    public FragmentReference get() {
        return new FragmentReferenceImpl();
    }
}
