package io.smallrye.graphql.index.app.namespace;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;

@GraphQLApi
@Name("name")
public class ApiWithName {
    @Query
    public String query() {
        return "query";
    }
}
