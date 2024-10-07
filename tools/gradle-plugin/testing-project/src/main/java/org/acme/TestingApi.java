package org.acme;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

@GraphQLApi
class TestingApi {
    @Query
    public Foo getFoo() {
        return null;
    }
}
