package io.smallrye.graphql.client.impl.typesafe.cdi;

import jakarta.enterprise.context.spi.CreationalContext;

import org.jboss.logging.Logger;

import io.smallrye.graphql.client.typesafe.api.TypesafeGraphQLClientBuilder;

public class TypesafeGraphQLClientBean<T> extends AbstractBean<T> {
    private static final Logger log = Logger.getLogger(TypesafeGraphQLClientBean.class);

    public TypesafeGraphQLClientBean(Class<T> apiClass) {
        super(apiClass);
    }

    @Override
    public T create(CreationalContext<T> creationalContext) {
        log.debugf("create GraphQL Client proxy: %s", type);
        return TypesafeGraphQLClientBuilder.newBuilder().build(type);
    }
}
