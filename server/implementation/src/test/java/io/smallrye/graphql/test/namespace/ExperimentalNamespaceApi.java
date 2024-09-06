package io.smallrye.graphql.test.namespace;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

import io.smallrye.graphql.api.Namespace;

@GraphQLApi
@Namespace({ "admin", "users" })
public class ExperimentalNamespaceApi {
    @Query
    public String find() {
        return "AdminUsersFind";
    }
}
