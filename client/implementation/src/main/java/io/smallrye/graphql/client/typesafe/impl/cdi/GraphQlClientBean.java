package io.smallrye.graphql.client.typesafe.impl.cdi;

import javax.enterprise.context.spi.CreationalContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.smallrye.graphql.client.typesafe.api.GraphQlClientBuilder;

public class GraphQlClientBean<T> extends AbstractBean<T> {
    private static final Logger log = LoggerFactory.getLogger(GraphQlClientBean.class);

    public GraphQlClientBean(Class<T> apiClass) {
        super(apiClass);
    }

    @Override
    public T create(CreationalContext<T> creationalContext) {
        log.debug("create GraphQL Client proxy: {}", type);
        return GraphQlClientBuilder.newBuilder().build(type);
    }
}
