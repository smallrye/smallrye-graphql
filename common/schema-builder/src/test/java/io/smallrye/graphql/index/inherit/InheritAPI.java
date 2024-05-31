package io.smallrye.graphql.index.inherit;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

@GraphQLApi
public class InheritAPI {
    @Query
    public ContainerInterface getContainer() {
        return null;
    }
}
