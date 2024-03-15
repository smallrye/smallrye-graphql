package io.smallrye.graphql.index.duplicates.typename;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

@GraphQLApi
public class ApiWithTypeNameDuplicates {

    @Query
    public io.smallrye.graphql.index.duplicates.typename.a.Animal queryA() {
        return null;
    }

    @Query
    public io.smallrye.graphql.index.duplicates.typename.b.Animal queryB() {
        return null;
    }
}
