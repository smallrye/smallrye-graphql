package io.smallrye.graphql.tests.objectid;

import org.bson.types.ObjectId;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

import io.smallrye.graphql.api.AdaptWith;

@GraphQLApi
public class SomeApi {
    @Query
    public @AdaptWith(ObjectIdAdapter.class) ObjectId returnObjectId(@AdaptWith(ObjectIdAdapter.class) ObjectId id) {
        return id;
    }

}
