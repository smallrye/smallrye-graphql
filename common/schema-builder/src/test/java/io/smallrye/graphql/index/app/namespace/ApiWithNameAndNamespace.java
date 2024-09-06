package io.smallrye.graphql.index.app.namespace;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;

import io.smallrye.graphql.api.Namespace;

@GraphQLApi
@Name("name")
@Namespace("namespace")
public class ApiWithNameAndNamespace {
    @Query
    public String query() {
        return "query";
    }
}
