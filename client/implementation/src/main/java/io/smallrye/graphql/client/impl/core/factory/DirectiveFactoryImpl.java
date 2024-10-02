package io.smallrye.graphql.client.impl.core.factory;

import io.smallrye.graphql.client.core.Directive;
import io.smallrye.graphql.client.core.factory.DirectiveFactory;
import io.smallrye.graphql.client.impl.core.DirectiveImpl;

public class DirectiveFactoryImpl implements DirectiveFactory {

    @Override
    public Directive get() {
        return new DirectiveImpl();
    }
}
