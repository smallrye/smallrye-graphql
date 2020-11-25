package io.smallrye.graphql.test.apps.variables.api;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

@GraphQLApi
public class VariablesTesingApi {

    @Query
    public String foo(Integer i) {
        return "bar_" + i;
    }

}
