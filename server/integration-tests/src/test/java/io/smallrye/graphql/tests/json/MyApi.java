package io.smallrye.graphql.tests.json;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

@GraphQLApi
public class MyApi {

    @Query
    public DateWrapper echo(DateWrapper dateWrapper) {
        return dateWrapper;
    }

}
