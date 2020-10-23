package io.smallrye.graphql.index.duplicates;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

@GraphQLApi
public class ApiWithDuplicates {

    @Query
    public io.smallrye.graphql.index.duplicates.a.Animal queryA() {
        return null;
    }

    @Query
    public io.smallrye.graphql.index.duplicates.b.Animal queryB() {
        return null;
    }
}
