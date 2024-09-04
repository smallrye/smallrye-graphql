package io.smallrye.graphql.test.namespace;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;

@Name("NamedNamespaceWithGroupingKey")
@GraphQLApi
public class NamedNamespaceWithGroupingKeyTestApi {
    @Query
    public NamedNamespaceWIthGroupingKeyModel getById(String id, String anotherId) {
        return new NamedNamespaceWIthGroupingKeyModel(id, anotherId, id);
    }
}
