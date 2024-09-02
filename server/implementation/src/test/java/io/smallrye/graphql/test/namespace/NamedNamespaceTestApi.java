package io.smallrye.graphql.test.namespace;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;

@Name("NamedNamespace")
@GraphQLApi
public class NamedNamespaceTestApi {
    @Query
    public NamedNamespaceModel getById(String id) {
        return new NamedNamespaceModel(id, id);
    }
}
