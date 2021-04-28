package io.smallrye.graphql.client.generator;

import java.lang.annotation.Repeatable;

@Repeatable(GraphQLQueries.class)
public @interface GraphQLQuery {
    String value();
}
