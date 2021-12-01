package io.smallrye.graphql.tests.client.typesafe.uni;

import org.eclipse.microprofile.graphql.Query;

import io.smallrye.graphql.client.typesafe.api.GraphQLClientApi;
import io.smallrye.mutiny.Uni;

@GraphQLClientApi
public interface UniClientApi {

    @Query
    Uni<String> asyncQuery();

    // This actually points to the same server-side query as `asyncQuery` method, but
    // the return type here is not async. The client library should not care.
    @Query("asyncQuery")
    String getSync();

    @Query("syncQuery")
    Uni<String> asyncMethodForSyncQuery();

}
