package io.smallrye.graphql.index.app.namespace;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

import io.smallrye.graphql.api.Namespace;

@GraphQLApi
@Namespace("namespace")
public class ApiWithNamespace {
    @Query
    public String query() {
        return "query";
    }
}
