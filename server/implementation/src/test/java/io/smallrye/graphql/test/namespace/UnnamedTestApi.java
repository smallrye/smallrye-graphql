package io.smallrye.graphql.test.namespace;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

@GraphQLApi
public class UnnamedTestApi {
    @Query
    public UnamedModel getById(String id) {
        return new UnamedModel(id, id);
    }
}
