package io.smallrye.graphql.test.apps.fieldexistence.api;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

@GraphQLApi
public class FieldExistenceApi {

    @Query
    public FieldExistencePojo fieldExistencePojo(FieldExistencePojo fieldExistencePojo) {
        return new FieldExistencePojo();
    }

}
