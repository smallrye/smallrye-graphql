package io.smallrye.graphql.tests.client.typesafe.uni;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

import io.smallrye.mutiny.Uni;

@GraphQLApi
public class UniApi {

    @Query
    public Uni<String> asyncQuery() {
        return Uni.createFrom().item("async");
    }

    @Query
    public String syncQuery() {
        return "sync";
    }

}
