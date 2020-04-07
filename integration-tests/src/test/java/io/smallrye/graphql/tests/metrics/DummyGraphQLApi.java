package io.smallrye.graphql.tests.metrics;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

@GraphQLApi
public class DummyGraphQLApi {

    @Query(value = "hello")
    public String helloQuery() {
        return "foo";
    }

    @Query(value = "mutate")
    public String mutation() {
        return "foo";
    }

}
