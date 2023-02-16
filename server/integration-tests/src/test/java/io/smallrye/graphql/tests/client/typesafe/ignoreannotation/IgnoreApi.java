package io.smallrye.graphql.tests.client.typesafe.ignoreannotation;

import java.util.List;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

import io.smallrye.graphql.tests.client.typesafe.ignoreannotation.servermodels.Person;

@GraphQLApi
public class IgnoreApi {
    @Query
    public Person getPerson() {
        return new Person(314_159_265, "Adam", "Smith", List.of(
                new Person(),
                new Person(),
                new Person()));
    }
}
