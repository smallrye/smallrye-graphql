package io.smallrye.graphql.client.generator.test;

import io.smallrye.graphql.client.generator.GraphQLQuery;
import io.smallrye.graphql.client.generator.GraphQLSchema;

@GraphQLSchema("resource:schema.graphql")
@GraphQLQuery("{ teams { name } }")
@GraphQLQuery("query heroesLocatedIn($location: String) { heroesIn(location: $location) { name realName superPowers } }")
public class SuperHeroes {
}
