package io.smallrye.graphql.client.impl.core.factory;

import io.smallrye.graphql.client.core.Document;
import io.smallrye.graphql.client.core.factory.DocumentFactory;
import io.smallrye.graphql.client.impl.core.DocumentImpl;

public class DocumentFactoryImpl implements DocumentFactory {

    @Override
    public Document get() {
        return new DocumentImpl();
    }
}
