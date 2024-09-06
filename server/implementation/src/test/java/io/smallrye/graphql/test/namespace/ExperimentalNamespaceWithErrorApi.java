package io.smallrye.graphql.test.namespace;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;

import io.smallrye.graphql.api.Namespace;

@GraphQLApi
@Name("users")
@Namespace({ "admin", "users" })
public class ExperimentalNamespaceWithErrorApi {
    @Query
    public String find() {
        return "AdminUsersFind";
    }
}
