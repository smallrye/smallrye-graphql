package io.smallrye.graphql.test.apps.enumlist.api;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

@GraphQLApi
public class EnumListApi {

    @Query
    public ObjectWithEnumList getObjectWithEnumList() {
        return new ObjectWithEnumList();
    }

}
