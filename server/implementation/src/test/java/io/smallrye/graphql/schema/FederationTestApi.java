package io.smallrye.graphql.schema;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

@GraphQLApi
public class FederationTestApi {
    @Query
    public TestTypeWithFederation testTypeWithFederation(String arg) {
        return null;
    }
}
