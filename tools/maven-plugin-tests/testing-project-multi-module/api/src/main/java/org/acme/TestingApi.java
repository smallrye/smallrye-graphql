package org.acme;

import org.acme.model.Foo;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

import java.util.Collections;
import java.util.List;

@GraphQLApi
class TestingApi {

    @Query
    public Foo getFoo() {
        return null;
    }


}