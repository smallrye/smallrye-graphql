package io.smallrye.graphql.client.generator.test;

import io.smallrye.graphql.client.generator.GraphQlSchema;
import io.smallrye.graphql.client.generator.GraphqlQuery;

@GraphQlSchema("resource:schema.graphql")
@GraphqlQuery("{ teams { name } }")
@GraphqlQuery("query heroesLocatedIn($location: String) { heroesIn(location: $location) { name realName superPowers } }")
public class SuperHeroes {
}
