package io.smallrye.graphql.client.impl.core.factory;

import io.smallrye.graphql.client.core.DirectiveArgument;
import io.smallrye.graphql.client.core.factory.DirectiveArgumentFactory;
import io.smallrye.graphql.client.impl.core.DirectiveArgumentImpl;

public class DirectiveArgumentFactoryImpl implements DirectiveArgumentFactory {

    @Override
    public DirectiveArgument get() {
        return new DirectiveArgumentImpl();
    }
}
