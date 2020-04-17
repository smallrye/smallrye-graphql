package io.smallrye.graphql.client.typesafe.impl.cdi;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.CDI;

import io.smallrye.graphql.client.typesafe.api.GraphQlClientBuilder;
import io.smallrye.graphql.client.typesafe.api.GraphQlClientHeader;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GraphQlClientBean<T> extends AbstractBean<T> {
    public GraphQlClientBean(Class<T> apiClass) {
        super(apiClass);
    }

    @Override
    public T create(CreationalContext<T> creationalContext) {
        log.debug("create GraphQL Client proxy: {}", type);
        GraphQlClientBuilder builder = GraphQlClientBuilder.newBuilder();
        CDI.current().select(GraphQlClientHeader.class).stream().forEach(builder::header);
        return builder.build(type);
    }
}
