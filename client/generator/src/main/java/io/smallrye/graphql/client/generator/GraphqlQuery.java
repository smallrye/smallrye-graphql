package io.smallrye.graphql.client.generator;

import java.lang.annotation.Repeatable;

@Repeatable(GraphqlQueries.class)
public @interface GraphqlQuery {
    String value();
}
